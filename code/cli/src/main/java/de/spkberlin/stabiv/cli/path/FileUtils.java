package de.spkberlin.stabiv.cli.path;

import de.spkberlin.stabiv.cli.configuration.state.ConfigurationInputSection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Find all files and directories that need to be validated.
     *
     * @param config the main yaml configuration
     * @return all directories and their files that need to be validated
     * @throws IOException in case filepaths cannot be evaluated
     */
    public static Map<Path, List<Path>> resolveFiles(ConfigurationInputSection config) throws IOException {
        logger.info("resolve files and directories");
        List<PathPattern> includePattern = config
            .getIncludes()
            .stream()
            .map(p -> new PathPattern(p.getPath(), p.getBaseDirectory()))
            .toList();
        List<PathPattern> excludePattern = config
            .getExcludes()
            .stream()
            .map(p -> new PathPattern(p.getPath(), p.getBaseDirectory()))
            .toList();

        List<Path> resolvedFiles = PathPatternResolver.resolveMany(includePattern, excludePattern);

        if (resolvedFiles.isEmpty()) {
            logger.warn("no files to validate");
        }

        return FileUtils.filterParentDirectoriesWithoutFiles(FileUtils.mapFilesByDirectory(resolvedFiles));
    }

    /**
     * Group all files by their respective parent directory.
     *
     * @param files the list of filepaths
     * @return a map from parent directory to files
     */
    public static Map<Path, List<Path>> mapFilesByDirectory(List<Path> files) {
        Map<Path, List<Path>> filesByDirectory = new TreeMap<>();
        for (Path path : files) {
            if (Files.isDirectory(path)) {
                // path is directory
                if (!filesByDirectory.containsKey(path)) {
                    filesByDirectory.put(path, new ArrayList<>());
                }
            } else {
                // path is file
                Path parent = path.getParent();
                if (!filesByDirectory.containsKey(parent)) {
                    filesByDirectory.put(parent, new ArrayList<>());
                }
                filesByDirectory.get(parent).add(path);
            }
        }
        return filesByDirectory;
    }

    /**
     * Filter directories that do not contain any files but are parent directories of other directories.
     *
     * @param filesByDirectory the map of directories and their files
     */
    public static Map<Path, List<Path>> filterParentDirectoriesWithoutFiles(Map<Path, List<Path>> filesByDirectory) {
        Set<Path> parentDirectories = filesByDirectory
            .keySet()
            .stream()
            .map(p -> p.getParent())
            .collect(Collectors.toSet());

        return filesByDirectory
            .entrySet()
            .stream()
            .filter(e -> !(e.getValue().isEmpty() && parentDirectories.contains(e.getKey())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entryLeft, entryRight) -> entryLeft, TreeMap::new));
    }

    /**
     * Make a filepath unique such that a file is never overwritten. Instead, add a number to the filename.
     *
     * @param filepath the filepath that is checked and modified to not exist yet
     * @return the modified filepath
     */
    public static Path modifyPathIfExists(Path filepath) {
        if (!Files.exists(filepath)) {
            // filepath doesn't exist, no modification necessary
            return filepath;
        }

        String filename = filepath.getFileName().toString();

        String basename = FilenameUtils.getBaseName(filename);
        String extension = FilenameUtils.getExtension(filename);

        int i = 1;
        while (true) {
            String candidateName = extension.isEmpty()
                ? String.format("%s (%d)", basename, i)
                : String.format("%s (%d).%s", basename, i, extension);
            Path candidate = filepath.resolveSibling(candidateName);

            if (!Files.exists(candidate)) {
                return candidate;
            }

            i++;
        }
    }

    /**
     * Walk a directory and match files based on include and exclude path matchers.
     *
     * @param directory the directory
     * @param includeMatcher a include matcher
     * @param excludeMatcher a list of exclude matcher
     * @return the list of matching paths
     * @throws IOException in case filepath is not available
     */
    public static List<Path> walkDirectory(Path directory, PathMatcher includeMatcher, List<PathMatcher> excludeMatcher)
        throws IOException {
        logger.trace("start walking directory '{}'", directory);
        List<Path> matches = Files.walk(directory)
            .map(p -> {
                logger.trace("scan file path '{}'", p);
                return p;
            })
            .filter(p -> Files.isRegularFile(p) || Files.isDirectory(p))
            .filter(p -> Objects.isNull(includeMatcher) || !p.equals(directory))
            .filter(p -> Objects.isNull(includeMatcher) || includeMatcher.matches(p))
            .filter(p -> !excludeMatcher.stream().anyMatch(m -> m.matches(p)))
            .toList();
        logger.trace("matched paths are: {}", matches);
        return matches;
    }
}
