package de.spkberlin.stabiv.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExactPatternResolver {

    private static final Logger logger = LoggerFactory.getLogger(GlobPatternResolver.class);

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();
    private static String REPLACE_REGEXP = "([\\\\*\\?\\[\\]{}])";

    /**
     * Return a base directory and path matcher for a exact path pattern.
     *
     * @param pattern the path pattern
     * @param cwd the current working directory
     * @return the path matcher
     */
    public static Pair<Path, PathMatcher> getPathMatcher(String pattern, Path cwd) {
        Path path = cwd.resolve(pattern).toAbsolutePath().normalize();
        Path baseDirectory = Files.isDirectory(path) ? path : path.getParent();
        String globPattern = buildGlobPattern(path);
        logger.trace(
            "exact path pattern '{}' transformed to glob pattern '{}' using base directory '{}'",
            pattern,
            globPattern,
            baseDirectory
        );
        return Pair.of(baseDirectory, FileSystems.getDefault().getPathMatcher(globPattern));
    }

    /**
     * Resolve an exact include path. If a file is provided exclude rules are ignored. If a directory is provided, it is scanned for all
     * containing files considering exclude rules.
     *
     * @param includePattern the include pattern referencing either a single file or a single directory
     * @param baseDirectory the base directory in case of relative paths
     * @param excludeMatcher the list of exclude rules as PathMatcher instances
     * @return the list of matches files
     * @throws IOException in case something fails scanning for files
     */
    public static List<Path> resolve(String includePattern, Path baseDirectory, List<PathMatcher> excludeMatcher) throws IOException {
        Path path = baseDirectory.resolve(includePattern).toAbsolutePath().normalize();

        if (Files.isRegularFile(path)) {
            return List.of(path);
        }

        Pair<Path, PathMatcher> includePair = getPathMatcher(includePattern, baseDirectory);
        return FileUtils.walkDirectory(includePair.getKey(), includePair.getValue(), excludeMatcher);
    }

    /**
     * Escape a simple pattern such that it can be used with a Java PathMatcher via the glob syntax.
     *
     * @param path the path to the file or directory
     * @return the escaped pattern to be used with the PathMatcher class and glob syntax
     */
    public static String buildGlobPattern(Path path) {
        if (Files.isDirectory(path)) {
            return String.format("glob:%s", (path.toString() + DIR_SEPERATOR).replaceAll(REPLACE_REGEXP, "\\\\$1")) + "**";
        }
        return String.format("glob:%s", path.toString().replaceAll(REPLACE_REGEXP, "\\\\$1"));
    }
}
