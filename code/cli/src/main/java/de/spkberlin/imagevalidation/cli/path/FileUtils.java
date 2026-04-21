package de.spkberlin.imagevalidation.cli.path;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationInputSection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
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
        List<GlobPattern> includePattern = config
            .getIncludes()
            .stream()
            .map(p -> new GlobPattern(p.getPath(), p.getBaseDirectory()))
            .toList();
        List<GlobPattern> excludePattern = config
            .getExcludes()
            .stream()
            .map(p -> new GlobPattern(p.getPath(), p.getBaseDirectory()))
            .toList();

        List<Path> resolvedFiles = GlobResolver.resolveMany(includePattern, excludePattern);

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
}
