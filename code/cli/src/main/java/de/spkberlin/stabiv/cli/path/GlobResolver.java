package de.spkberlin.stabiv.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobResolver {

    private static final Logger logger = LoggerFactory.getLogger(GlobResolver.class);

    private static Path CWD = Path.of(System.getProperty("user.dir"));
    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();
    private static String[] GLOB_PATTERN_CHARACTERS = { "*", "?", "[", "{" };

    /**
     * Resolve multiple path patterns to a deduplicated list of files assuming the current working directory as base path.
     *
     * @param includePattern the list of path patterns to resolve
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<GlobPattern> includePattern) throws IOException {
        return resolveMany(includePattern, Collections.emptyList(), CWD);
    }

    /**
     * Resolve multiple path patterns to a deduplicated list of files assuming the current working directory as base path.
     *
     * @param includePattern the list of path patterns to resolve
     * @param excludePattern a list of path patterns that should be excluded
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<GlobPattern> includePattern, List<GlobPattern> excludePattern) throws IOException {
        return resolveMany(includePattern, excludePattern, CWD);
    }

    /**
     * Resolve multiple path patterns to a deduplicated list of files.
     *
     * @param includePattern the list of path patterns to resolve
     * @param excludePattern a list of path patterns that should be excluded
     * @param defaultBaseDirectory the default base directory in case a pattern doesn't specify a base directory
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<GlobPattern> includePattern, List<GlobPattern> excludePattern, Path defaultBaseDirectory)
        throws IOException {
        return includePattern
            .stream()
            .map(p -> {
                try {
                    logger.debug("resolve path pattern '{}'", p.getPattern());
                    return resolveOne(p, excludePattern, defaultBaseDirectory);
                } catch (NoSuchFileException e) {
                    logger.error("no such file '{}'", e.getMessage());
                } catch (IOException e) {
                    logger.error("exception resolving file pattern", e);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    /**
     * Resolve a path pattern to a list of matching files assuming the current working directory as base path.
     *
     * @param includePattern the path pattern to be resolved
     * @return the list of files matching the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveOne(GlobPattern includePattern) throws IOException {
        return resolveOne(includePattern, Collections.emptyList(), CWD);
    }

    /**
     * Resolve a path pattern to a list of matching files assuming the current working directory as base path.
     *
     * @param includePattern the path pattern to be resolved
     * @param excludePattern a list of path patterns that should be excluded
     * @return the list of files matching the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveOne(GlobPattern includePattern, List<GlobPattern> excludePattern) throws IOException {
        return resolveOne(includePattern, excludePattern, CWD);
    }

    /**
     * Resolve a path pattern to a list of matching files.
     *
     * @param includePattern the path pattern to be resolved
     * @param excludePattern a list of path patterns that should be excluded
     * @param defaultBaseDirectory the working directory (in case the path pattern is relative)
     * @return the list of files matching the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveOne(GlobPattern includePattern, List<GlobPattern> excludePattern, Path defaultBaseDirectory)
        throws IOException {
        Path includeBaseDirectory = Objects.nonNull(includePattern.getBaseDirectory())
            ? includePattern.getBaseDirectory()
            : defaultBaseDirectory;
        Pair<Path, String> splittedPattern = splitGlobPattern(includePattern.getPattern(), includeBaseDirectory);
        Path basePath = splittedPattern.getKey();
        String globSuffix = splittedPattern.getValue();

        PathMatcher includeMatcher = null;
        if (!globSuffix.isEmpty()) {
            String absolutePattern = joinGlobPattern(splittedPattern);
            includeMatcher = getPathMatcherFromPattern(includePattern.getPattern(), includeBaseDirectory);
            logger.debug("resolve include glob '{}' for base directory '{}'", absolutePattern, basePath);
        } else {
            logger.debug("resolve anything for path '{}'", basePath);
        }

        List<PathMatcher> excludeMatcher = excludePattern
            .stream()
            .map(p ->
                getPathMatcherFromPattern(
                    p.getPattern(),
                    Objects.nonNull(p.getBaseDirectory()) ? p.getBaseDirectory() : defaultBaseDirectory
                )
            )
            .toList();

        return walkDirectory(basePath, includeMatcher, excludeMatcher);
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

    /**
     * Return a path matcher for a path pattern.
     *
     * @param pattern the path pattern
     * @param cwd the current working directory
     * @return the patch matcher
     */
    public static PathMatcher getPathMatcherFromPattern(String pattern, Path cwd) {
        Pair<Path, String> splittedPattern = splitGlobPattern(pattern, cwd);
        String absolutePattern = joinGlobPattern(splittedPattern);
        logger.trace("build path matcher based on absolute pattern '{}'", absolutePattern);
        return FileSystems.getDefault().getPathMatcher("glob:" + absolutePattern.replace("\\", "\\\\"));
    }

    /**
     * Return true if a path pattern is an absolute path pattern.
     *
     * @param pattern the path pattern to check
     * @return true if the path pattern is absolute
     */
    public static boolean isPatternAbsolute(String pattern) {
        return splitGlobPattern(pattern, Path.of(".")).getKey().isAbsolute();
    }

    /**
     * In case a path pattern is relative, resolve it to an absolute path pattern.
     *
     * @param pattern the path pattern (either relative or absolute)
     * @return absolute path pattern
     */
    public static String makePatternAbsolute(String pattern) {
        return joinGlobPattern(splitGlobPattern(pattern, CWD));
    }

    /**
     * In case a path pattern is relative, resolve it to an absolute path pattern.
     *
     * @param pattern the path pattern (either relative or absolute)
     * @param cwd the current working directory
     * @return absolute path pattern
     */
    public static String makePatternAbsolute(String pattern, Path cwd) {
        return joinGlobPattern(splitGlobPattern(pattern, cwd));
    }

    /**
     * Join a splitted glob pattern back together into a string.
     *
     * @param splittedPattern the splitted glob pattern
     * @return the joined string
     */
    public static String joinGlobPattern(Pair<Path, String> splittedPattern) {
        if (splittedPattern.getRight().isEmpty()) {
            return splittedPattern.getKey().toString();
        }
        return String.format("%s%s%s", splittedPattern.getLeft(), DIR_SEPERATOR, splittedPattern.getRight());
    }

    /**
     * Split a glob pattern into its fixed base path and an optional glob pattern suffix.
     *
     * <p>If no glob pattern is found, an empty string is returned.
     *
     * @param pattern the glob pattern
     * @param baseDirectory the current working directory (in case the path pattern is a relative path)
     * @return a pair of the absolute base path (a directory or file) and the optional remaining glob pattern
     */
    public static Pair<Path, String> splitGlobPattern(String pattern, Path baseDirectory) {
        int firstGlobCharIdx = findFirstGlobCharIndex(pattern);

        if (firstGlobCharIdx == pattern.length()) {
            // there is no glob pattern
            return Pair.of(baseDirectory.resolve(Path.of(pattern)).normalize(), "");
        }

        // there is glob character somewhere
        Integer lastSeperatorIdx = pattern.substring(0, firstGlobCharIdx).lastIndexOf(DIR_SEPERATOR);
        String directoryPrefix = pattern.substring(0, Math.max(0, lastSeperatorIdx));
        String globSuffix = pattern.substring(lastSeperatorIdx + 1, pattern.length());

        return Pair.of(baseDirectory.resolve(Path.of(directoryPrefix)).normalize(), globSuffix);
    }

    /**
     * Return the index of the first character in the string that is interpreted as glob pattern. If no such character is found, the string
     * is not a pattern at all, and the total length of the string is returned instead.
     *
     * @param pattern the string to check
     * @return the index of the first character that corresponds to a glob pattern, or the size of the string if no such character is found
     */
    public static int findFirstGlobCharIndex(String pattern) {
        return Arrays.stream(GLOB_PATTERN_CHARACTERS)
            .map(c -> pattern.indexOf(c))
            .filter(i -> i >= 0)
            .reduce(Integer::min)
            .orElse(pattern.length());
    }

    /**
     * Return true of the string contains a glob pattern.
     *
     * @param pattern the string to check
     * @return true if it is a glob pattern, else false
     */
    public static boolean isGlobPattern(String pattern) {
        return findFirstGlobCharIndex(pattern) != pattern.length();
    }
}
