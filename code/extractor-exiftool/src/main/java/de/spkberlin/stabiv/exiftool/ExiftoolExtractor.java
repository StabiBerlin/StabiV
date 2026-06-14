package de.spkberlin.stabiv.exiftool;

import de.spkberlin.stabiv.common.extract.ExtractionError;
import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.extract.Extractor;
import de.spkberlin.stabiv.common.math.NumberConsecutiveness;
import de.spkberlin.stabiv.common.properties.PathPropertiesTree;
import de.spkberlin.stabiv.common.properties.PathProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the ExifTool and parses the output it into an hierarchical object of extracted properties and their values.
 *
 * <p>The output of ExifTool must be in the format <code>[prefix] name : value</code>, e.g.,
 * <code>[TIFF-IFD0] ImageWidth : 100</code>.</p>
 *
 * <p>Properties that are consistently suffixed with a array index are converted to arrays, e.g., <code>TIFF-IFD0</code> and
 * <code>TIFF-IFD1</code> are considered an array of two IFD subproperties. In contrast, if the index is not consecutive, e.g.,
 * <code>TIFF-Property0</code> and <code>TIFF-Property2</code>, the property is not considered an array but individual subproperties.</p>
 */
public class ExiftoolExtractor implements Extractor {

    public static final String DEFAULT_CMD = "exiftool";
    public static final List<String> DEFAULT_CLI_ARGS = List.of("-a", "-u", "-s", "-G5");
    public static final List<String> VERSION_ARGS = List.of("-ver");

    private static final Logger logger = LoggerFactory.getLogger(ExiftoolExtractor.class);

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\s*(.+)");
    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*?)([0-9]+)?$");
    private static final String HIERARCHY_SEPARATOR = "-";
    private static final String PROPERTY_VALUE_SEPARATOR = ":";

    private String cmd;
    private List<String> cliArgs;

    /**
     * Initialize an ExifTool extractor with a custom command and custom options.
     *
     * @param cmd the ExifTool command to be executed (default "exiftool")
     * @param cliArgs the list of command line arguments for ExifTool (default "-a -u -s -G5")
     */
    public ExiftoolExtractor(String cmd, List<String> cliArgs) {
        this.cmd = cmd;
        this.cliArgs = cliArgs;
    }

    public ExtractionResult extractProperties(Path filename) throws ExtractionError {
        String output = runExifTool(filename);
        return new ExtractionResult(parseExifToolOutput(output));
    }

    public String getVersion() {
        return getExifVersion();
    }

    /**
     * Initialize ExifTool extractor with default options.
     */
    public ExiftoolExtractor() {
        this(DEFAULT_CMD, DEFAULT_CLI_ARGS);
    }

    /**
     * Initialize an ExifTool extractor with custom command line arguments (usually <code>-a -u -s -G5</code>).
     *
     * @param cliArgs the list of command line arguments for ExifTool
     */
    public ExiftoolExtractor(List<String> cliArgs) {
        this(DEFAULT_CMD, cliArgs);
    }

    /**
     * Retrieves the version of Exiftool by running "exiftool -ver" and parsing the version from stdout.
     *
     * @return the version string or "unknown version" in case a problem occurred
     */
    public String getExifVersion() {
        StringBuilder stdout = new StringBuilder();
        try {
            List<String> command = Stream.of(Stream.of(this.cmd), VERSION_ARGS.stream())
                .flatMap(s -> s)
                .toList();

            Process process = new ProcessBuilder(command).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line);
                    stdout.append(NEWLINE);
                }
            }

            return stdout.toString().trim();
        } catch (IOException e) {
            logger.error("exception while determining Exiftool version", e);
            return "unknown version";
        }
    }

    /**
     * Spawn a new process for ExifTool and collect both stdout and stderr.
     *
     * @param filename the filepath of the file to process with ExifTool
     * @return the stdout of ExifTool
     * @throws ExtractionError in case something failed or there is data transmitted on stderr
     */
    public String runExifTool(Path filename) throws ExtractionError {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        try {
            List<String> command = Stream.of(Stream.of(this.cmd), this.cliArgs.stream(), Stream.of(filename.toString()))
                .flatMap(s -> s)
                .toList();

            logger.trace("start process '{}'", command.stream().collect(Collectors.joining(" ")));
            Process process = new ProcessBuilder(command).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line);
                    stdout.append(NEWLINE);
                }
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line);
                    stderr.append(NEWLINE);
                }
            }

            logger.trace("exiftool stdout was:\n\n{}", stdout.toString());
            if (!stderr.isEmpty()) {
                logger.trace("exiftool stderr was:\n\n{}", stderr.toString());
                throw new ExtractionError("exiftool failed with stderr: " + stderr.toString());
            }
        } catch (IOException e) {
            logger.trace("error starting exiftool", e);
            throw new ExtractionError("exiftool failed to run with: " + e.getMessage());
        }
        return stdout.toString();
    }

    /**
     * Parse the output of Exiftool to an object tree by reading each line as a property value pair.
     *
     * @param output the stdout of the ExifTool assuming options "-a -u -s -G5"
     * @return the parsed property tree
     */
    public Map<String, Object> parseExifToolOutput(String output) throws ExtractionError {
        try {
            List<String> lines = Arrays.asList(output.split(NEWLINE));

            // split lines into properties and values
            List<Pair<String, String>> splittedLines = lines
                .stream()
                .map(ExiftoolExtractor::splitPropertyValueLine)
                .filter(Objects::nonNull)
                .toList();

            // find all prefixes with consistent array indexes
            Set<String> arrayPropertyPrefixSet = arrayPropertyPrefixSet(
                propertyPrefixSet(
                    splittedLines
                        .stream()
                        .map(p -> p.getLeft())
                        .toList()
                )
            );

            // parse each line to PathProperty instances
            Set<PathProperty> paths = splittedLines
                .stream()
                .map(lineSplit -> new PathProperty(parsePropertyToPath(lineSplit.getLeft(), arrayPropertyPrefixSet), lineSplit.getRight()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            // convert list of properties into object tree
            return PathPropertiesTree.buildTreeFromPaths(paths);
        } catch (Exception e) {
            logger.trace("error during parsing of ExitTool output", e);
            throw new ExtractionError(e);
        }
    }

    /**
     * Convert a property provided by ExifTool into a path similar to XPath.
     *
     * <p>For example, `[TIFF-IFD0] XResolution` is converted to `TIFF/IFD[0]/XResolution`.
     *
     * @param property the property as provided by ExifTool
     * @param arrayPropertyPrefixSet the set of prefixes that are known to be consistent array properties
     * @return the converted property as path similar to an XPath
     */
    public static String parsePropertyToPath(String property, Set<String> arrayPropertyPrefixSet) {
        return PROPERTY_PATTERN.matcher(property).replaceAll(match -> {
            String propertyPrefix = transformPropertyPrefix(match.group(1), arrayPropertyPrefixSet);
            String propertyName = match.group(2);
            return String.format("%s/%s", propertyPrefix, propertyName);
        });
    }

    /**
     * Transform a property prefix into a path similar to XPath.
     *
     * <p>For example, `TIFF-IFD0` is converted to `TIFF/IFD[0]`.
     *
     * @param prefix the property prefix, e.g., <code>TIFF-IFD0</code>
     * @param arrayPropertyPrefixSet the set of prefixes that are known to be consistent array properties
     * @return the converted property prefix as path similar to an XPath
     */
    public static String transformPropertyPrefix(String prefix, Set<String> arrayPropertyPrefixSet) {
        List<String> levels = Arrays.asList(prefix.split(HIERARCHY_SEPARATOR));
        return IntStream.range(0, levels.size())
            .mapToObj(i -> {
                String level = levels.get(i);
                String ancestorPath = levels.subList(0, i + 1).stream().collect(Collectors.joining(HIERARCHY_SEPARATOR));
                Pair<String, Integer> levelArrayIndex = extractPotentialArrayIndex(level);
                Pair<String, Integer> ancestorPathArrayIndex = extractPotentialArrayIndex(ancestorPath);
                if (arrayPropertyPrefixSet.contains(ancestorPathArrayIndex.getLeft())) {
                    return String.format("%s[%d]", levelArrayIndex.getLeft(), ancestorPathArrayIndex.getRight());
                }
                return level;
            })
            .collect(Collectors.joining("/"));
    }

    /**
     * Return the set of property prefixes that are consistent array properties.
     *
     * <p>Consistent array properties have a number at the end, and there is an occurrence for each index from 0 to n without gaps.
     *
     * @param prefixSet the set of all property prefixes
     * @return the set of property prefixes that are consistent array properties
     */
    public static Set<String> arrayPropertyPrefixSet(Set<String> prefixSet) {
        // iterate through all property prefixes and collect numbers for each prefix
        Map<String, Set<Integer>> propertiesWithIndexes = new HashMap<>();
        for (String prefix : prefixSet) {
            Pair<String, Integer> potentialArrayIndex = extractPotentialArrayIndex(prefix);

            if (!propertiesWithIndexes.containsKey(potentialArrayIndex.getKey())) {
                propertiesWithIndexes.put(potentialArrayIndex.getKey(), new HashSet<>());
            }
            propertiesWithIndexes.get(potentialArrayIndex.getKey()).add(potentialArrayIndex.getValue());
        }

        // check indexes are consistent (from 0 to n without gaps)
        return propertiesWithIndexes
            .entrySet()
            .stream()
            .filter(e -> {
                List<Integer> sorted = e.getValue().stream().filter(Objects::nonNull).sorted().toList();
                boolean isConsecutive = NumberConsecutiveness.findGaps(sorted, 0).isEmpty();
                return !e.getValue().contains(null) && isConsecutive;
            })
            .map(e -> e.getKey())
            .collect(Collectors.toSet());
    }

    /**
     * Return the set of property prefixes including prefixes of all sublevels.
     *
     * @param properties the property part of ExifTool output lines
     * @return the set of prefix property paths
     */
    public static Set<String> propertyPrefixSet(List<String> properties) {
        Set<String> prefixSet = properties.stream().map(ExiftoolExtractor::extractPropertyPrefix).collect(Collectors.toSet());

        Set<String> withSubLevels = new HashSet<>(prefixSet);
        for (String prefix : prefixSet) {
            List<String> parts = Arrays.asList(prefix.split(HIERARCHY_SEPARATOR));
            for (int i = 1; i <= parts.size(); i++) {
                withSubLevels.add(parts.subList(0, i).stream().collect(Collectors.joining(HIERARCHY_SEPARATOR)));
            }
        }

        return withSubLevels;
    }

    /**
     * Parse a single output line of the exiftool output and split it into a pair of property and value.
     *
     * @param line the output line
     * @return a pair of property and value part, or null, if not properly formatted
     */
    public static Pair<String, String> splitPropertyValueLine(String line) {
        String[] propertyArray = line.split(PROPERTY_VALUE_SEPARATOR, 2);
        if (propertyArray.length != 2) {
            logger.trace("exiftool output doesn't fit pattern of 'property:value':\n{}", line);
            return null;
        }

        return Pair.of(propertyArray[0].trim(), propertyArray[1].trim());
    }

    /**
     * Extract the prefix part for a property.
     *
     * @param linePropertyPart the property (e.g. <code>[TIFF-IFD0] ImageWidth</code>) of a ExifTool output line
     * @return the prefix part of the property, e.g. <code>TIFF-IFD0</code>
     */
    public static String extractPropertyPrefix(String linePropertyPart) {
        Matcher matcher = PROPERTY_PATTERN.matcher(linePropertyPart);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("cannot extract property from exiftool line '%s'", linePropertyPart));
        }
        return matcher.group(1);
    }

    /**
     * Extract the potential array index from a property prefix.
     *
     * @param prefix the property prefix, e.g., <code>TIFF-IFD0</code>
     * @return the pair of the base prefix and the extracted index, e.g., <code>TIFF-IFD</code> and index 0
     */
    public static Pair<String, Integer> extractPotentialArrayIndex(String prefix) {
        Matcher matcher = ARRAY_PATTERN.matcher(prefix);
        matcher.matches();
        String pathPart = matcher.group(1);
        Integer numberPart = Objects.nonNull(matcher.group(2)) ? Integer.valueOf(matcher.group(2)) : null;

        return Pair.of(pathPart, numberPart);
    }
}
