package de.spkberlin.imagevalidation.exiftool;

import de.spkberlin.imagevalidation.common.extract.ExtractionError;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.common.properties.PathPropertiesTree;
import de.spkberlin.imagevalidation.common.properties.PathProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExiftoolExtractor implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(ExiftoolExtractor.class);

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String EXIFTOOL_CMD = "exiftool";
    private static final String[] EXIFTOOL_ARGS = { "-a", "-u", "-s", "-G5" };

    private static final Pattern LINE_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\s*(.+)");
    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*?)([0-9]+)?$");

    public Map<String, Object> extractProperties(Path filename) throws ExtractionError {
        String output = runExifTool(filename);
        return parseExifToolOutput(output);
    }

    public String runExifTool(Path filename) throws ExtractionError {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        try {
            List<String> command = Stream.of(Stream.of(EXIFTOOL_CMD), Arrays.stream(EXIFTOOL_ARGS), Stream.of(filename.toString()))
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
            logger.error("error starting exiftool", e);
            throw new ExtractionError("exiftool failed to run with: " + e.getMessage());
        }
        return stdout.toString();
    }

    public Map<String, Object> parseExifToolOutput(String output) {
        Set<PathProperty> properties = new HashSet<>();
        String[] lines = output.split(NEWLINE);

        for (String line : lines) {
            String[] propertyArray = line.split(":", 2);

            if (propertyArray.length != 2) {
                logger.trace("exiftool output doesn't fit pattern of 'property:value':\n{}", line);
                continue;
            }

            String propertyPath = transformPropertyName(propertyArray[0].trim());
            String propertyValue = propertyArray[1].trim();

            properties.add(new PathProperty(propertyPath, propertyValue));
        }

        return PathPropertiesTree.buildTreeFromPaths(properties);
    }

    public static String transformArrayProperty(String part) {
        return ARRAY_PATTERN.matcher(part).replaceAll(m -> m.group(1) + (Objects.nonNull(m.group(2)) ? ("[" + m.group(2) + "]") : ""));
    }

    public static String transformPropertyName(String encoded) {
        return LINE_PATTERN.matcher(encoded).replaceAll(match -> {
            String propertyPath = Arrays.asList(match.group(1).split("-"))
                .stream()
                .map(part -> transformArrayProperty(part))
                .collect(Collectors.joining("/"));
            String propertyName = match.group(2);
            return String.format("%s/%s", propertyPath, propertyName);
        });
    }
}
