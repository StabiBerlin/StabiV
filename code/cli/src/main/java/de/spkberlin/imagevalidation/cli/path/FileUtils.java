package de.spkberlin.imagevalidation.cli.path;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationInputSection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

        Map<Path, List<Path>> filesByDirectory = FileUtils.mapFilesByDirectory(resolvedFiles);

        return filesByDirectory;
    }

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
}
