package de.spkberlin.stabiv.cli.path;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Distinguishes the 3 different path pattern types that are supported: simple pattern, full glob pattern, full regex pattern.
 */
public enum PathPatternType {
    /**
     * The provided string exactly matches a regular file or directory. No interpretation required.
     */
    EXACT,

    /**
     * A simple path pattern only supports stars to match multiple characters, e.g. <code>*.tif</code> or * <code>**.tif</code>.
     * Escaping of these stars is not supported.
     */
    SIMPLE,

    /**
     * A glob path pattern starts with the prefix "glob:" and supports all glob features of the Java PathMatcher class, see:
     * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
     */
    GLOB,

    /**
     * A regex path pattern starts with the prefix "regex:" and supports matching via a regular expression, see:
     * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html
     */
    REGEX;

    /**
     * Determine the type of a pattern string based on its prefix.
     *
     * @param pattern the pattern string
     * @return the glob pattern type
     */
    public static PathPatternType getType(String pattern, Path baseDirectory) {
        if (Objects.isNull(pattern)) {
            throw new IllegalArgumentException();
        }
        try {
            if (Files.exists(baseDirectory.resolve(pattern))) {
                return PathPatternType.EXACT;
            }
        } catch (InvalidPathException e) {
            // ignore
        }
        if (pattern.startsWith(RegexPatternResolver.REGEX_PREFIX)) {
            return PathPatternType.REGEX;
        } else if (pattern.startsWith(GlobPatternResolver.GLOB_PREFIX)) {
            return PathPatternType.GLOB;
        }
        return PathPatternType.SIMPLE;
    }
}
