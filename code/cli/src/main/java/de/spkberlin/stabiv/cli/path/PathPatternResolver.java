package de.spkberlin.stabiv.cli.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathPatternResolver {

    private static final Logger logger = LoggerFactory.getLogger(PathPatternResolver.class);

    private static Path CWD = Path.of(System.getProperty("user.dir"));

    /**
     * Resolve multiple path patterns to a deduplicated list of files assuming the current working directory as base path.
     *
     * @param includePattern the list of path patterns to resolve
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<PathPattern> includePattern) throws IOException {
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
    public static List<Path> resolveMany(List<PathPattern> includePattern, List<PathPattern> excludePattern) throws IOException {
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
    public static List<Path> resolveMany(List<PathPattern> includePattern, List<PathPattern> excludePattern, Path defaultBaseDirectory)
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
    public static List<Path> resolveOne(PathPattern includePattern) throws IOException {
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
    public static List<Path> resolveOne(PathPattern includePattern, List<PathPattern> excludePattern) throws IOException {
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
    public static List<Path> resolveOne(PathPattern includePattern, List<PathPattern> excludePattern, Path defaultBaseDirectory)
        throws IOException {
        Pair<Path, PathMatcher> includePair = pathPatternToPathMatcher(includePattern, defaultBaseDirectory);
        Path basePath = includePair.getKey();
        PathMatcher includeMatcher = includePair.getValue();

        if (Files.isRegularFile(basePath)) {
            // include pattern references single regular file
            return List.of(basePath);
        }

        List<PathMatcher> excludeMatcher = excludePattern
            .stream()
            .map(p ->
                pathPatternToPathMatcher(p, Objects.nonNull(p.getBaseDirectory()) ? p.getBaseDirectory() : defaultBaseDirectory).getValue()
            )
            .toList();

        return walkDirectory(basePath, includeMatcher, excludeMatcher);
    }

    /**
     * Determines the type of pattern and returns an absolute base path and path matcher for the pattern.
     *
     * @param pattern the pattern to be resolved
     * @param defaultBaseDirectory the working directory (in case the path pattern is relative)
     * @return a pair of absolute base path and path matcher
     */
    public static Pair<Path, PathMatcher> pathPatternToPathMatcher(PathPattern pattern, Path defaultBaseDirectory) {
        Path baseDirectory = Objects.nonNull(pattern.getBaseDirectory()) ? pattern.getBaseDirectory() : defaultBaseDirectory;

        PathPatternType patternType = PathPatternType.getType(pattern.getPattern(), baseDirectory);
        if (patternType.equals(PathPatternType.EXACT)) {
            return ExactPatternResolver.getPathMatcher(pattern.getPattern(), baseDirectory);
        } else if (patternType.equals(PathPatternType.SIMPLE)) {
            return SimplePatternResolver.getPathMatcher(pattern.getPattern(), baseDirectory);
        } else if (patternType.equals(PathPatternType.GLOB)) {
            return GlobPatternResolver.getPathMatcher(pattern.getPattern(), baseDirectory);
        } else if (patternType.equals(PathPatternType.REGEX)) {
            return RegexPatternResolver.getPathMatcher(pattern.getPattern(), baseDirectory);
        }

        throw new IllegalArgumentException("unknown type of include pattern");
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
        logger.error("start walking directory '{}'", directory);
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
