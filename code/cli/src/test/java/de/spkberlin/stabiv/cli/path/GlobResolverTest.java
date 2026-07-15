package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.spkberlin.stabiv.cli.path.GlobPattern;
import de.spkberlin.stabiv.cli.path.GlobResolver;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class GlobResolverTest {

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();

    @TempDir
    private Path tempDir;

    @Test
    public void testIsGlobPattern() {
        // pattern
        assertTrue(GlobResolver.isGlobPattern("/test/*.tif"));
        assertTrue(GlobResolver.isGlobPattern("/test/image.{jpg,tif}"));
        assertTrue(GlobResolver.isGlobPattern("/test/**/original/*.{jpg,tif}"));

        // regular filepaths
        assertFalse(GlobResolver.isGlobPattern("/test/image.tif"));
        assertFalse(GlobResolver.isGlobPattern("/test/directory/"));
    }

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
    public void testResolveSingleFile() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(new GlobPattern(fileB.toString(), tempDir));

        assertFalse(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveAbsolutePattern() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(new GlobPattern("*.tif", tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveRelativePattern() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(new GlobPattern(String.format(".%s*.tif", DIR_SEPERATOR), tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveDirectory() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(new GlobPattern(tempDir.toString(), tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertTrue(matched.contains(fileC));
        assertTrue(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveSubdirectory() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(new GlobPattern(GlobResolver.joinGlobPattern(Pair.of(tempDir, "**")), tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertTrue(matched.contains(fileC));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolveWithExclude() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("file1.tif"));
        Path fileB = Files.createFile(tempDir.resolve("file2.tif"));
        Path fileC = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(
            new GlobPattern(tempDir.toString(), tempDir),
            List.of(new GlobPattern("*.jpg", tempDir))
        );

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertTrue(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolvePatternMatchedSubdirectoryExactly() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(
            new GlobPattern(GlobResolver.joinGlobPattern(Pair.of(tempDir, "*directory")), tempDir)
        );

        assertFalse(matched.contains(fileA));
        assertFalse(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.WINDOWS })
    public void testResolvePatternMatchedSubdirectoryWithFiles() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = GlobResolver.resolveOne(
            new GlobPattern(GlobResolver.joinGlobPattern(Pair.of(tempDir, "*directory**")), tempDir)
        );

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertTrue(matched.contains(fileC));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }
}
