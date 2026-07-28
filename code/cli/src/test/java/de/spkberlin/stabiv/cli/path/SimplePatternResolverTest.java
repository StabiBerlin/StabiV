package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class SimplePatternResolverTest {

    /**
     * Check that the base directory is correctly extracted from a simple path pattern.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testSplitPatternBasePathLinux() {
        Path cwd = Path.of("/directory");

        // patterns
        assertEquals("/directory", SimplePatternResolver.splitPattern("/directory/*.tif", cwd).getKey().toString());
        assertEquals("/directory", SimplePatternResolver.splitPattern("/directory/**.tif", cwd).getKey().toString());
        assertEquals("/directory", SimplePatternResolver.splitPattern("*.tif", cwd).getKey().toString());

        // exact files
        assertEquals("/directory/test.tif", SimplePatternResolver.splitPattern("test.tif", cwd).getKey().toString());

        // escaping is not supported (backslash is treaded as filename character)
        assertEquals("/directory", SimplePatternResolver.splitPattern("/directory/\\*.tif", cwd).getKey().toString());
    }

    /**
     * Check that the base directory is correctly extracted from a simple path pattern.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testSplitPatternBasePathWindows() {
        Path cwd = Path.of("C:\\directory");

        // patterns
        assertEquals("C:\\directory", SimplePatternResolver.splitPattern("C:\\directory\\*.tif", cwd).getKey().toString());
        assertEquals("C:\\directory", SimplePatternResolver.splitPattern("C:\\directory\\**.tif", cwd).getKey().toString());
        assertEquals("C:\\directory", SimplePatternResolver.splitPattern("*.tif", cwd).getKey().toString());

        // exact files
        assertEquals("C:\\directory\\test.tif", SimplePatternResolver.splitPattern("test.tif", cwd).getKey().toString());

        // escaping is not supported (backslash is treaded as filename character)
        assertEquals("C:\\directory", SimplePatternResolver.splitPattern("C:\\directory\\\\*.tif", cwd).getKey().toString());
    }

    /**
     * Check that the glob suffix is correctly extracted from a simple path pattern.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testSplitPatternGlobSuffixLinux() {
        Path cwd = Path.of("/directory");

        // patterns
        assertEquals("*.tif", SimplePatternResolver.splitPattern("/directory/*.tif", cwd).getValue());
        assertEquals("**.tif", SimplePatternResolver.splitPattern("/directory/**.tif", cwd).getValue());
        assertEquals("*.tif", SimplePatternResolver.splitPattern("*.tif", cwd).getValue());

        // exact files
        assertEquals("", SimplePatternResolver.splitPattern("test.tif", cwd).getValue());

        // escaping is not supported (backslash is treaded as filename character)
        assertEquals("\\*.tif", SimplePatternResolver.splitPattern("/directory/\\*.tif", cwd).getValue());
    }

    /**
     * Check that the glob suffix is correctly extracted from a simple path pattern.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testSplitPatternGlobSuffixWindows() {
        Path cwd = Path.of("C:\\directory");

        // patterns
        assertEquals("*.tif", SimplePatternResolver.splitPattern("C:\\directory\\*.tif", cwd).getValue());
        assertEquals("**.tif", SimplePatternResolver.splitPattern("C:\\directory\\**.tif", cwd).getValue());
        assertEquals("*.tif", SimplePatternResolver.splitPattern("*.tif", cwd).getValue());

        // exact files
        assertEquals("", SimplePatternResolver.splitPattern("test.tif", cwd).getValue());

        // escaping is not supported (backslash is treaded as filename character)
        assertEquals("\\*.tif", SimplePatternResolver.splitPattern("C:\\directory\\\\*.tif", cwd).getValue());
    }
}
