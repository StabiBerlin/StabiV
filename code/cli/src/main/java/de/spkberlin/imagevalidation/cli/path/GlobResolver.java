package de.spkberlin.imagevalidation.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobResolver {

    private static final Logger logger = LoggerFactory.getLogger(GlobResolver.class);

    private static String GLOB_PATTERN_CHARACTERS = "*?[{";

    /**
     * Resolve multiple path patterns to a deduplicated list of files assuming the current working directory as base path.
     *
     * @param pattern the list of path patterns to resolve
     * @return the list of files matching any of the path pattern
     * @throws IOException in case the file system cannot be accessed
     */
    public static List<Path> resolveMany(List<Path> pattern) throws IOException {
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
    public static List<Path> resolveMany(List<Path> pattern, Path cwd) throws IOException {
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
    public static List<Path> resolveOne(Path pattern) throws IOException {
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
    public static List<Path> resolveOne(Path pattern, Path cwd) throws IOException {
        Path absolutePattern = makePatternAbsolute(pattern, cwd);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + absolutePattern);
        Path baseDir = findPatternBasePath(absolutePattern, cwd);
        logger.debug("resolve path pattern '{}' for base directory '{}'", absolutePattern, baseDir);
        return Files.walk(baseDir)
            .map(p -> {
                logger.trace("match file path '{}'", p);
                return p;
            })
            .filter(Files::isRegularFile)
            .filter(matcher::matches)
            .toList();
    }

    /**
     * Return true if a path pattern is an absolute path pattern.
     *
     * @param pattern the path pattern to check
     * @return true if the path pattern is absolute
     */
    public static boolean isPatternAbsolute(Path pattern) {
        return pattern.isAbsolute();
    }

    /**
     * In case a path pattern is relative, resolve it to an absolute path pattern.
     *
     * @param pattern the path pattern (either relative or absolute)
     * @param cwd the current working directory
     * @return absolute path pattern
     */
    public static Path makePatternAbsolute(Path pattern, Path cwd) {
        if (isPatternAbsolute(pattern)) {
            return pattern;
        }

        return cwd.resolve(pattern).normalize();
    }

    /**
     * Returns the base path that needs to be walked recursively to find all files of a path pattern.
     *
     * @param pattern the path pattern
     * @param cwd the current working directory (in case the path pattern is a relative path)
     * @return the base path of the path pattern
     */
    public static Path findPatternBasePath(Path pattern, Path cwd) {
        // start with root component of path (e.g. / or C://)
        Path basePath = Objects.nonNull(pattern.getRoot()) ? pattern.getRoot() : cwd;

        // iterate over all path parts until a part contains glob components
        boolean found = false;
        for (Path part : pattern) {
            String partString = part.toString();
            for (char c : GLOB_PATTERN_CHARACTERS.toCharArray()) {
                if (partString.contains(String.valueOf(c))) {
                    // glob pattern character found in path
                    found = true;
                    break;
                }
            }

            if (found) {
                break;
            }

            // add part to base path if it doesn't contain any glob characters
            basePath = basePath.resolve(part);
        }

        // normalize base path
        return cwd.resolve(basePath).normalize();
    }
}
