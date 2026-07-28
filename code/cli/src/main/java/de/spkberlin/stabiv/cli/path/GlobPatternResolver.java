package de.spkberlin.stabiv.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;
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
     * Resolve a glob include pattern considering exclude rules.
     *
     * @param includePattern the include pattern as a glob path pattern
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
     * Join a splitted glob pattern back together into a string.
     *
     * @param splittedPattern the splitted glob pattern
     * @return the joined string
     */
    public static String buildGlobPattern(Pair<Path, String> splittedPattern) {
        if (splittedPattern.getRight().isEmpty()) {
            return String.format("glob:%s", escapePath(splittedPattern.getKey().toString()));
        }
        return String.format(
            "glob:%s%s%s",
            escapePath(splittedPattern.getKey().toString()),
            escapePath(DIR_SEPERATOR),
            splittedPattern.getRight()
        );
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
            Path path = baseDirectory.toAbsolutePath().resolve(Path.of(unescapePattern(patternWithoutPrefix))).normalize();
            if (Files.isRegularFile(path)) {
                return Pair.of(path.getParent(), path.getFileName().toString());
            }
            return Pair.of(path, "");
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
            .map(c -> firstIndexOfUnescapedCharacter(pattern, c.charAt(0), ESCAPE_CHARACTER.charAt(0)))
            .filter(i -> i >= 0)
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

    /**
     * Escapes any glob pattern characters from the path part of a glob path pattern that should not be interpreted as glob characters.
     *
     * @param path the path containing characters that need escaping
     * @return the path with escaped characters
     */
    public static String escapePath(String path) {
        return path.replaceAll("([\\\\*\\?\\[\\]{}])", "\\\\$1");
    }
}
