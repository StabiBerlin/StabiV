package de.spkberlin.stabiv.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePatternResolver {

    private static final Logger logger = LoggerFactory.getLogger(GlobPatternResolver.class);

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();
    private static String STAR_CHARACTER = "*";

    /**
     * Return a base directory and path matcher for a simple path pattern.
     *
     * @param pattern the path pattern
     * @param cwd the current working directory
     * @return the path matcher
     */
    public static Pair<Path, PathMatcher> getPathMatcher(String pattern, Path cwd) {
        Pair<Path, String> splittedPattern = splitPattern(pattern, cwd);
        String globPattern = buildGlobPattern(joinPattern(splittedPattern));
        Path baseDirectory = splittedPattern.getKey();
        logger.trace(
            "simple path pattern '{}' transformed to glob pattern '{}' using base directory '{}'",
            pattern,
            globPattern,
            baseDirectory
        );
        return Pair.of(splittedPattern.getKey(), FileSystems.getDefault().getPathMatcher(globPattern));
    }

    /**
     * Resolve a simple include pattern considering exclude rules.
     *
     * @param includePattern the include pattern referencing as simple path pattern
     * @param baseDirectory the base directory in case of relative paths
     * @param excludeMatcher the list of exclude rules as PathMatcher instances
     * @return the list of matches files
     * @throws IOException in case something fails scanning for files
     */
    public static List<Path> resolve(String includePattern, Path baseDirectory, List<PathMatcher> excludeMatcher) throws IOException {
        Pair<Path, PathMatcher> includePair = getPathMatcher(includePattern, baseDirectory);
        return FileUtils.walkDirectory(includePair.getKey(), includePair.getValue(), excludeMatcher);
    }

    /**
     * Escape a simple pattern such that it can be used with a Java PathMatcher via the glob syntax.
     *
     * @param pattern the simple pattern (only supporting star characters, other glob characters are escaped)
     * @return the escaped pattern to be used with the PathMatcher class
     */
    public static String buildGlobPattern(String pattern) {
        return "glob:" + pattern.replaceAll("([\\\\?\\[\\]{}])", "\\\\$1");
    }

    /**
     * Join a splitted simple pattern back together into a string.
     *
     * @param splittedPattern the splitted simple pattern
     * @return the joined string
     */
    public static String joinPattern(Pair<Path, String> splittedPattern) {
        if (splittedPattern.getRight().isEmpty()) {
            return splittedPattern.getKey().toString();
        }
        return String.format("%s%s%s", splittedPattern.getLeft(), DIR_SEPERATOR, splittedPattern.getRight());
    }

    /**
     * Split a simple pattern into its absolute base path and an optional pattern suffix (including star symbols).
     *
     * @param pattern the simple pattern
     * @param baseDirectory the current working directory (in case the path pattern is a relative path)
     * @return a pair of the absolute base path (a directory or file) and the optional remaining glob pattern
     */
    public static Pair<Path, String> splitPattern(String pattern, Path baseDirectory) {
        int firstStarCharIdx = pattern.indexOf(STAR_CHARACTER);

        if (firstStarCharIdx == -1) {
            // there is no star symbol in the pattern
            return Pair.of(baseDirectory.resolve(Path.of(pattern)).toAbsolutePath().normalize(), "");
        }

        // there is star symbol somewhere
        Integer lastSeperatorIdx = pattern.substring(0, firstStarCharIdx).lastIndexOf(DIR_SEPERATOR);
        String directoryPrefix = pattern.substring(0, Math.max(0, lastSeperatorIdx));
        String patternSuffix = pattern.substring(lastSeperatorIdx + 1, pattern.length());

        return Pair.of(baseDirectory.resolve(Path.of(directoryPrefix)).toAbsolutePath().normalize(), patternSuffix);
    }
}
