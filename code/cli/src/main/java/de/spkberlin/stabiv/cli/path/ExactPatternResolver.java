package de.spkberlin.stabiv.cli.path;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExactPatternResolver {

    private static final Logger logger = LoggerFactory.getLogger(GlobPatternResolver.class);

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();

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
        String globPattern = Files.isDirectory(path) ? buildGlobPattern(path) + DIR_SEPERATOR + "**" : buildGlobPattern(path);
        logger.trace(
            "exact path pattern '{}' transformed to glob pattern '{}' using base directory '{}'",
            pattern,
            globPattern,
            baseDirectory
        );
        return Pair.of(baseDirectory, FileSystems.getDefault().getPathMatcher(globPattern));
    }

    /**
     * Escape a simple pattern such that it can be used with a Java PathMatcher via the glob syntax.
     *
     * @param path the path to the file or directory
     * @return the escaped pattern to be used with the PathMatcher class and glob syntax
     */
    public static String buildGlobPattern(Path path) {
        return String.format("glob:%s", path.toString().replaceAll("([\\\\*\\?\\[\\]{}])", "\\\\$1"));
    }
}
