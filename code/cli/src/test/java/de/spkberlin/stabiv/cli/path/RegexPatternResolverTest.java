package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class RegexPatternResolverTest {

    /**
     * Check that simple regular expression matching works in Linux.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testRegexPatternMatchingLinux() {
        // exact path
        assertTrue(
            RegexPatternResolver.getPathMatcher("regex:^/directory/test\\.tif", Path.of("/directory"))
                .getValue()
                .matches(Path.of("/directory/test.tif"))
        );

        // any file inside the directory
        assertTrue(
            RegexPatternResolver.getPathMatcher("regex:^/directory/[^/]+\\.tif", Path.of("/directory"))
                .getValue()
                .matches(Path.of("/directory/test.tif"))
        );

        // not files within a subdirectory
        assertFalse(
            RegexPatternResolver.getPathMatcher("regex:^/directory/[^/]+\\.tif", Path.of("/directory"))
                .getValue()
                .matches(Path.of("/directory/subdirectory/test.tif"))
        );
    }

    /**
     * Check that simple regular expression matching works in Linux.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testRegexPatternMatchingWindows() {
        // exact path
        assertTrue(
            RegexPatternResolver.getPathMatcher("regex:^C:\\\\directory\\\\test\\.tif", Path.of("C:\\directory"))
                .getValue()
                .matches(Path.of("C:\\directory\\test.tif"))
        );

        // any file inside the directory
        assertTrue(
            RegexPatternResolver.getPathMatcher("regex:^C:\\\\directory\\\\[^\\\\]+\\.tif", Path.of("C:\\directory"))
                .getValue()
                .matches(Path.of("C:\\directory\\test.tif"))
        );

        // not files within a subdirectory
        assertFalse(
            RegexPatternResolver.getPathMatcher("regex:^C:\\\\directory\\\\[^\\\\]+\\.tif", Path.of("C:\\directory"))
                .getValue()
                .matches(Path.of("C:\\directory\\subdirectory\\test.tif"))
        );
    }
}
