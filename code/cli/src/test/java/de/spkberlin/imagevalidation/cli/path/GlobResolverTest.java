package de.spkberlin.imagevalidation.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class GlobResolverTest {

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();

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
    void testMakePatternAbsoluteLinux() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.makePatternAbsolute("./test/*.tif", cwd), "/tmp/test/*.tif");
    }

    @Test
    @EnabledOnOs({ OS.WINDOWS })
    void testMakePatternAbsoluteWindows() {
        Path cwd = Path.of("C:\\tmp");
        assertEquals(GlobResolver.makePatternAbsolute(".\\test\\*.tif", cwd), "C:\\tmp\\test\\*.tif");
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testSplitPatternBasePathLinux() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.splitGlobPattern("./test/*.tif", cwd).getKey().toString(), "/tmp/test");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/*.tif", cwd).getKey().toString(), "/custom/test");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/123.tif", cwd).getKey().toString(), "/custom/test/123.tif");
    }

    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testSplitPatternBasePathWindows() {
        Path cwd = Path.of("C:\\tmp");
        assertEquals(GlobResolver.splitGlobPattern(".\\test\\*.tif", cwd).getKey().toString(), "C:\\tmp\\test");
        assertEquals(GlobResolver.splitGlobPattern("C:\\custom\\test\\*.tif", cwd).getKey().toString(), "C:\\custom\\test");
        assertEquals(GlobResolver.splitGlobPattern("C:\\custom\\test\\123.tif", cwd).getKey().toString(), "C:\\custom\\test\\123.tif");
    }

    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testSplitPatternGlobSuffixLinux() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.splitGlobPattern("./test/*.tif", cwd).getValue(), "*.tif");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/*.tif", cwd).getValue(), "*.tif");
        assertEquals(GlobResolver.splitGlobPattern("/custom/test/123.tif", cwd).getValue(), "");
    }

    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testSplitPatternGlobSuffixWindows() {
        Path cwd = Path.of("C:\\tmp");
        assertEquals(GlobResolver.splitGlobPattern(".\\test\\*.tif", cwd).getValue(), "*.tif");
        assertEquals(GlobResolver.splitGlobPattern("C:\\custom\\test\\*.tif", cwd).getValue(), "*.tif");
        assertEquals(GlobResolver.splitGlobPattern("C:\\custom\\test\\123.tif", cwd).getValue(), "");
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
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
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveRelativePattern() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(String.format(".%s*.tif", DIR_SEPERATOR), Collections.emptyList(), tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertFalse(matched.contains(file3));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveDirectory() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(tempDir.toString(), Collections.emptyList(), tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertTrue(matched.contains(file3));
        assertTrue(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveSubdirectory() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path file1 = Files.createFile(subdirectory.resolve("file1.tif"));
        Path file2 = Files.createFile(subdirectory.resolve("file2.tif"));
        Path file3 = Files.createFile(subdirectory.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(
            GlobResolver.joinGlobPattern(Pair.of(tempDir, "**")),
            Collections.emptyList(),
            tempDir
        );

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertTrue(matched.contains(file3));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveWithExclude() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(tempDir.toString(), List.of("*.jpg"), tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertFalse(matched.contains(file3));
        assertTrue(matched.contains(tempDir));
    }
}
