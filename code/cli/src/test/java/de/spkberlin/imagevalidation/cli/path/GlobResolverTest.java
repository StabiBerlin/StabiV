package de.spkberlin.imagevalidation.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class GlobResolverTest {

    @TempDir
    Path tempDir;

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testPatternAbsoluteLinux() {
        // absolute patterns
        assertTrue(GlobResolver.isPatternAbsolute("/test/test.tif"));
        assertTrue(GlobResolver.isPatternAbsolute("/test/*.tif"));
        assertTrue(GlobResolver.isPatternAbsolute("/test/**/original/*.{jpg,tif}"));

        // relative patterns
        assertFalse(GlobResolver.isPatternAbsolute("test/test.tif"));
        assertFalse(GlobResolver.isPatternAbsolute("./test/*.tif"));
        assertFalse(GlobResolver.isPatternAbsolute("../test/**/original/*.{jpg,tif}"));
    }

    @Test
    @Disabled
    @EnabledOnOs({ OS.WINDOWS })
    public void testPatternAbsoluteWindows() {
        // absolute patterns
        assertTrue(GlobResolver.isPatternAbsolute("C:\\test\\test.tif"));
        assertTrue(GlobResolver.isPatternAbsolute("C:\\test\\*.tif"));
        assertTrue(GlobResolver.isPatternAbsolute("C:\\test\\**\\original\\*.{jpg,tif}"));

        // relative patterns
        assertFalse(GlobResolver.isPatternAbsolute("test\\test.tif"));
        assertFalse(GlobResolver.isPatternAbsolute(".\\test\\*.tif"));
        assertFalse(GlobResolver.isPatternAbsolute("..\\test\\**\\original\\*.{jpg,tif}"));
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    void testMakePatternAbsolute() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.makePatternAbsolute("./test/*.tif", cwd), "/tmp/test/*.tif");
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testPatternBaseDirectory() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.splitGlobPattern("./test/*.tif", cwd).getKey().toString(), "/tmp/test");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/*.tif", cwd).getKey().toString(), "/custom/test");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/123.tif", cwd).getKey().toString(), "/custom/test/123.tif");
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testPatternGlobSuffix() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.splitGlobPattern("./test/*.tif", cwd).getValue(), "*.tif");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/*.tif", cwd).getValue(), "*.tif");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/123.tif", cwd).getValue(), "");
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testResolveAbsolutePattern() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(GlobResolver.joinGlobPattern(Pair.of(tempDir, "*.tif")));

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertFalse(matched.contains(file3));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testResolveRelativePattern() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne("./*.tif", tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertFalse(matched.contains(file3));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testResolveDirectory() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(tempDir.toString(), tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertTrue(matched.contains(file3));
        assertTrue(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testResolveSubdirectory() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path file1 = Files.createFile(subdirectory.resolve("file1.tif"));
        Path file2 = Files.createFile(subdirectory.resolve("file2.tif"));
        Path file3 = Files.createFile(subdirectory.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(GlobResolver.joinGlobPattern(Pair.of(tempDir, "**")), tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertTrue(matched.contains(file3));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }
}
