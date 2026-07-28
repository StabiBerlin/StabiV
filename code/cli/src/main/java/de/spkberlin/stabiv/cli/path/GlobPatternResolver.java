package de.spkberlin.stabiv.cli.path;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobPatternResolver {

    public static String GLOB_PREFIX = "glob:";

    private static final Logger logger = LoggerFactory.getLogger(GlobPatternResolver.class);
    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();
    private static String ESCAPE_CHARACTER = "\\";
    private static String[] GLOB_PATTERN_CHARACTERS = { "*", "?", "[", "{" };

    /**
     * Return a base directory and path matcher for a glob path pattern.
     *
     * @param pattern the glob path pattern
     * @param cwd the current working directory
     * @return the path matcher
     */
    public static Pair<Path, PathMatcher> getPathMatcher(String pattern, Path cwd) {
        Pair<Path, String> splittedPattern = splitPattern(pattern, cwd);
        String globPattern = buildGlobPattern(splittedPattern);
        Path baseDirectory = splittedPattern.getKey();
        logger.trace(
            "glob path pattern '{}' transformed to glob pattern '{}' with base directory '{}'",
            pattern,
            globPattern,
            baseDirectory
        );
        return Pair.of(baseDirectory, FileSystems.getDefault().getPathMatcher(globPattern));
    }

    /**
     * Join a splitted glob pattern back together into a string.
     *
     * @param splittedPattern the splitted glob pattern
     * @return the joined string
     */
    public static String buildGlobPattern(Pair<Path, String> splittedPattern) {
        if (splittedPattern.getRight().isEmpty()) {
            return String.format("glob:%s", splittedPattern.getKey().toString());
        }
        return String.format("glob:%s%s%s", splittedPattern.getLeft(), DIR_SEPERATOR, splittedPattern.getRight());
    }

    /**
     * Split a glob pattern into its base path and an optional glob pattern suffix.
     *
     * @param pattern the glob pattern (including the "glob:" prefix)
     * @param baseDirectory the current working directory (in case the path pattern is a relative path)
     * @return a pair of the absolute base path (a directory or file) and the optional remaining glob pattern
     */
    public static Pair<Path, String> splitPattern(String pattern, Path baseDirectory) {
        if (!pattern.startsWith(GLOB_PREFIX)) {
            throw new IllegalArgumentException("pattern needs to start with \"glob:\" prefix");
        }
        String patternWithoutPrefix = pattern.substring(GLOB_PREFIX.length());
        int firstGlobCharIdx = findFirstGlobCharIndex(patternWithoutPrefix);

        if (firstGlobCharIdx == patternWithoutPrefix.length()) {
            // there is no glob pattern
            return Pair.of(baseDirectory.toAbsolutePath().resolve(Path.of(unescapePattern(patternWithoutPrefix))).normalize(), "");
        }

        // there is glob character somewhere
        Integer lastSeperatorIdx = patternWithoutPrefix.substring(0, firstGlobCharIdx).lastIndexOf(DIR_SEPERATOR);
        String baseDirectoryPart = patternWithoutPrefix.substring(0, Math.max(0, lastSeperatorIdx));
        String globSuffix = patternWithoutPrefix.substring(lastSeperatorIdx + 1, patternWithoutPrefix.length());

        return Pair.of(baseDirectory.toAbsolutePath().resolve(Path.of(unescapePattern(baseDirectoryPart))).normalize(), globSuffix);
    }

    /**
     * Return the index of the first character in the string that is interpreted as glob pattern. If no such character is found, the string
     * is not a pattern at all, and the total length of the string is returned instead.
     *
     * @param pattern the glob path pattern to check (usually without "glob:" prefix)
     * @return the index of the first character that corresponds to a glob pattern, or the size of the string if no such character is found
     */
    public static int findFirstGlobCharIndex(String pattern) {
        return Arrays.stream(GLOB_PATTERN_CHARACTERS)
            .map(c -> pattern.indexOf(c))
            .filter(i -> i >= 0)
            .filter(i -> i == 0 || !pattern.substring(i - 1, i).equals(ESCAPE_CHARACTER))
            .reduce(Integer::min)
            .orElse(pattern.length());
    }

    /**
     * Find the first unescaped occurrence of a character in a search string.
     *
     * @param search the search string
     * @param target the target character to look for
     * @param escape the escape character
     * @return the index of the first unescaped occurrence of the target character or -1
     */
    public static int firstIndexOfUnescapedCharacter(String search, char target, char escape) {
        for (int i = 0; i < search.length(); i++) {
            if (search.charAt(i) != target) {
                continue;
            }

            // found target character, check for escaping
            int escapeCount = 0;
            for (int j = i - 1; j >= 0 && search.charAt(j) == escape; j--) {
                escapeCount++;
            }

            if ((escapeCount % 2) == 0) {
                // found even number of escapes
                // meaning target character is unescaped
                return i;
            }
        }

        return -1;
    }

    /**
     * Unescapes any glob pattern characters from the base path of a glob path pattern.
     *
     * @param basePath the base path containing escaped glob characters
     * @return the base path with unescaped characters
     */
    public static String unescapePattern(String basePath) {
        return basePath.replaceAll("\\\\([\\\\*\\?\\[\\]{}])", "$1");
    }
}
