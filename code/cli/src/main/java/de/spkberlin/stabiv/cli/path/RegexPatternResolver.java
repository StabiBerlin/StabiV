package de.spkberlin.stabiv.cli.path;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;

public class RegexPatternResolver {

    public static final String REGEX_PREFIX = "regex:";

    /**
     * Return a pair of base directory and path matcher for a regex path pattern.
     *
     * @param pattern the regex path pattern (including the "regex:" prefix)
     * @param baseDirectory the base directory, which is used to resolve the regex pattern
     * @return the pair of base directory and path matcher instance
     */
    public static Pair<Path, PathMatcher> getPathMatcher(String pattern, Path baseDirectory) {
        if (!pattern.startsWith(REGEX_PREFIX)) {
            throw new IllegalArgumentException("pattern needs to start with \"regex:\" prefix");
        }
        if (Objects.isNull(baseDirectory)) {
            throw new IllegalArgumentException("base directory cannot be null");
        }
        // do not modify or interpret pattern, but simply relay it to PathMatcher class
        return Pair.of(baseDirectory, FileSystems.getDefault().getPathMatcher(pattern));
    }

    /**
     * Resolve a regex include pattern considering exclude rules.
     *
     * @param includePattern the include pattern as a regex path pattern
     * @param baseDirectory the base directory in case of relative paths
     * @param excludeMatcher the list of exclude rules as PathMatcher instances
     * @return the list of matches files
     * @throws IOException in case something fails scanning for files
     */
    public static List<Path> resolve(String includePattern, Path baseDirectory, List<PathMatcher> excludeMatcher) throws IOException {
        Pair<Path, PathMatcher> includePair = getPathMatcher(includePattern, baseDirectory);
        return FileUtils.walkDirectory(includePair.getKey(), includePair.getValue(), excludeMatcher);
    }
}
