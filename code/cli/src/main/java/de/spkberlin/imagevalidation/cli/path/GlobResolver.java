package de.spkberlin.imagevalidation.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobResolver {

    private static final Logger logger = LoggerFactory.getLogger(GlobResolver.class);

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();
    private static String[] GLOB_PATTERN_CHARACTERS = { "*", "?", "[", "{" };

    /**
     * Resolve multiple path patterns to a deduplicated list of files assuming the current working directory as base path.
     *
     * @param pattern the list of path patterns to resolve
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<String> pattern) throws IOException {
        return resolveMany(pattern, Path.of(System.getProperty("user.dir")));
    }

    /**
     * Resolve multiple path patterns to a deduplicated list of files.
     *
     * @param pattern the list of path patterns to resolve
     * @param cwd the working directory (in case the path pattern is relative)
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<String> pattern, Path cwd) throws IOException {
        return pattern
            .stream()
            .map(p -> {
                try {
                    logger.debug("resolve path pattern '{}'", p);
                    return resolveOne(p, cwd);
                } catch (IOException e) {
                    logger.error("exception resolving file pattern", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    /**
     * Resolve a path pattern to a list of matching files assuming the current working directory as base path.
     *
     * @param pattern the path pattern to be resolved
     * @return the list of files matching the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveOne(String pattern) throws IOException {
        return resolveOne(pattern, Path.of(System.getProperty("user.dir")));
    }

    /**
     * Resolve a path pattern to a list of matching files.
     *
     * @param pattern the path pattern to be resolved
     * @param cwd the working directory (in case the path pattern is relative)
     * @return the list of files matching the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveOne(String pattern, Path cwd) throws IOException {
        List<IOException> exceptions = new ArrayList<>();
        Pair<Path, String> splittedPattern = splitGlobPattern(pattern, cwd);
        Path basePath = splittedPattern.getKey();
        String globSuffix = splittedPattern.getValue();

        PathMatcher includeMatcher = null;
        if (!globSuffix.isEmpty()) {
            String absolutePattern = joinGlobPattern(splittedPattern);
            includeMatcher = FileSystems.getDefault().getPathMatcher("glob:" + absolutePattern.replace("\\", "\\\\"));
            logger.debug("resolve glob '{}' for base directory '{}'", absolutePattern, basePath);
        } else {
            logger.debug("resolve anything for path '{}'", basePath);
        }

        List<Path> matches = walkDirectory(basePath, includeMatcher, exceptions);

        if (exceptions.size() > 0) {
            // rethrow IOException
            throw exceptions.get(0);
        }

        return matches;
    }

    private static List<Path> walkDirectory(Path directory, PathMatcher includeMatcher, List<IOException> exceptions) throws IOException {
        logger.trace("start walking directory '{}'", directory);
        List<Path> matches = Files.walk(directory)
            .map(p -> {
                logger.trace("scan file path '{}'", p);
                return p;
            })
            .filter(p -> Files.isRegularFile(p) || Files.isDirectory(p))
            .filter(p -> Objects.isNull(includeMatcher) || !p.equals(directory))
            .filter(p -> Objects.isNull(includeMatcher) || includeMatcher.matches(p))
            .toList();
        logger.trace("matched paths are: {}", matches);
        return matches;
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
        return String.format("%s%s%s", splittedPattern.getLeft(), DIR_SEPERATOR, splittedPattern.getRight());
    }

    /**
     * Split a glob pattern into its fixed base path and an optional glob pattern suffix.
     *
     * <p>If no glob pattern is found, an empty string is returned.
     *
     * @param pattern the glob pattern
     * @param cwd the current working directory (in case the path pattern is a relative path)
     * @return a pair of the absolute base path (a directory or file) and the optional remaining glob pattern
     */
    public static Pair<Path, String> splitGlobPattern(String pattern, Path cwd) {
        Integer firstGlobCharIdx = Arrays.stream(GLOB_PATTERN_CHARACTERS)
            .map(c -> pattern.indexOf(c))
            .filter(i -> i >= 0)
            .reduce(Integer::min)
            .orElse(pattern.length());

        if (firstGlobCharIdx.equals(pattern.length())) {
            // there is no glob pattern
            return Pair.of(cwd.resolve(Path.of(pattern)).normalize(), "");
        }

        // there is glob character somewhere
        Integer lastSeperatorIdx = pattern.substring(0, firstGlobCharIdx).lastIndexOf(DIR_SEPERATOR);
        String directoryPrefix = pattern.substring(0, Math.max(0, lastSeperatorIdx));
        String globSuffix = pattern.substring(lastSeperatorIdx + 1, pattern.length());

        return Pair.of(cwd.resolve(Path.of(directoryPrefix)).normalize(), globSuffix);
    }
}
