package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class GlobPatternResolverTest {

    /**
     * Check that first glob character index is calculated correctly.
     */
    @Test
    public void testFindFirstGlobCharIndex() {
        assertEquals(11, GlobPatternResolver.findFirstGlobCharIndex("glob:prefix*.tif"));
        assertEquals(17, GlobPatternResolver.findFirstGlobCharIndex("glob:prefix\\*.tif"));
        assertEquals(6, GlobPatternResolver.findFirstGlobCharIndex("glob:t[es]t.tif"));
        assertEquals(17, GlobPatternResolver.findFirstGlobCharIndex("glob:t\\[es\\]t.tif"));
    }

    /**
     * Check that base directory is correctly determined from glob pattern in Linux.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testSplitPatternBasePathLinux() {
        Path cwd = Path.of("/tmp");
        assertEquals("/tmp/test", GlobPatternResolver.splitPattern("glob:./test/*.tif", cwd).getKey().toString());
        assertEquals("/custom/test", GlobPatternResolver.splitPattern("glob:/custom/test/*.tif", cwd).getKey().toString());
        assertEquals("/custom/test/123.tif", GlobPatternResolver.splitPattern("glob:/custom/test/123.tif", cwd).getKey().toString());
    }

    /**
     * Check that base directory is correctly determined from glob pattern in Windows.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testSplitPatternBasePathWindows() {
        Path cwd = Path.of("C:\\tmp");
        assertEquals("C:\\tmp\\test", GlobPatternResolver.splitPattern("glob:.\\\\test\\\\*.tif", cwd).getKey().toString());
        assertEquals("C:\\custom\\test", GlobPatternResolver.splitPattern("glob:C:\\\\custom\\\\test\\\\*.tif", cwd).getKey().toString());
        assertEquals(
            "C:\\custom\\test\\123.tif",
            GlobPatternResolver.splitPattern("glob:C:\\\\custom\\\\test\\\\123.tif", cwd).getKey().toString()
        );
    }

    /**
     * Check that glob suffix is correctly determined from glob pattern in Linux.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testSplitPatternGlobSuffixLinux() {
        Path cwd = Path.of("/tmp");
        assertEquals("*.tif", GlobPatternResolver.splitPattern("glob:./test/*.tif", cwd).getValue());
        assertEquals("*.tif", GlobPatternResolver.splitPattern("glob:/custom/test/*.tif", cwd).getValue());
        assertEquals("image.{tif,jpg}", GlobPatternResolver.splitPattern("glob:/custom/test/image.{tif,jpg}", cwd).getValue());
        assertEquals("*.tif", GlobPatternResolver.splitPattern("glob:/custom\\[dir\\]/test/*.tif", cwd).getValue());
        assertEquals("", GlobPatternResolver.splitPattern("glob:/custom/test/123.tif", cwd).getValue());
    }

    /**
     * Check that glob suffix is correctly determined from glob pattern in Windows.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testSplitPatternGlobSuffixWindows() {
        Path cwd = Path.of("C:\\tmp");
        assertEquals("*.tif", GlobPatternResolver.splitPattern("glob:.\\\\test\\\\*.tif", cwd).getValue());
        assertEquals("*.tif", GlobPatternResolver.splitPattern("glob:C:\\\\custom\\\\test\\\\*.tif", cwd).getValue());
        assertEquals("", GlobPatternResolver.splitPattern("glob:C:\\\\custom\\\\test\\\\123.tif", cwd).getValue());
    }
}
