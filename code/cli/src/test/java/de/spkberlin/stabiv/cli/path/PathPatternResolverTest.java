package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class PathPatternResolverTest {

    private static String DIR_SEPERATOR = FileSystems.getDefault().getSeparator();

    @TempDir
    private Path tempDir;

    /**
     * Check that a glob pattern exactly referencing a specific file only matches that file.
     */
    @Test
    public void testResolveSingleFile() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(new PathPattern(fileB.toString(), tempDir));

        assertFalse(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that an absolute path with wildcard pattern matches only tif files.
     */
    @Test
    public void testResolveAbsolutePattern() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(
            new PathPattern(String.format("%s%s*.tif", tempDir.toString(), DIR_SEPERATOR), tempDir)
        );

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that a relative pattern with wildcard only matches tif files.
     */
    @Test
    public void testResolveRelativePattern() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(new PathPattern(String.format(".%s*.tif", DIR_SEPERATOR), tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that a glob pattern exactly referencing a directory matches all files in this directory.
     */
    @Test
    public void testResolveDirectory() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("fileA.tif"));
        Path fileB = Files.createFile(tempDir.resolve("fileB.tif"));
        Path fileC = Files.createFile(tempDir.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(new PathPattern(tempDir.toString(), tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertTrue(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that a double wildcard pattern matches all files of a subdirectory, including the subdirectory itself.
     */
    @Test
    public void testResolveSubdirectory() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(new PathPattern(String.format("%s%s**", tempDir, DIR_SEPERATOR), tempDir));

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertTrue(matched.contains(fileC));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that escaping glob characters in a relative path pattern works.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testResolveWithEscapedGlobCharactersLinux() throws IOException {
        Path subdirectory = tempDir.resolve("sub[dir]ectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(new PathPattern("glob:sub\\[dir\\]ectory/fileB.tif", tempDir));

        assertFalse(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that escaping glob characters in a relative path pattern works.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testResolveWithEscapedGlobCharactersWindows() throws IOException {
        Path subdirectory = tempDir.resolve("sub[dir]ectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(new PathPattern("glob:sub\\[dir\\]ectory\\\\fileB.tif", tempDir));

        assertFalse(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that files can be excluded via a wildcard pattern.
     */
    @Test
    public void testResolveWithExclude() throws IOException {
        Path fileA = Files.createFile(tempDir.resolve("file1.tif"));
        Path fileB = Files.createFile(tempDir.resolve("file2.tif"));
        Path fileC = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(
            new PathPattern(tempDir.toString(), tempDir),
            List.of(new PathPattern("*.jpg", tempDir))
        );

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that a wildcard pattern inside a directory name only matches the directory, but no other files.
     */
    @Test
    public void testResolvePatternMatchedSubdirectoryExactly() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(
            new PathPattern(String.format("%s%s*directory", tempDir, DIR_SEPERATOR), tempDir)
        );

        assertFalse(matched.contains(fileA));
        assertFalse(matched.contains(fileB));
        assertFalse(matched.contains(fileC));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }

    /**
     * Check that a wildcard pattern inside a directory with double wildcard at the end matches all files of the directory.
     */
    @Test
    public void testResolvePatternMatchedSubdirectoryWithFiles() throws IOException {
        Path subdirectory = tempDir.resolve("subdirectory");
        Files.createDirectory(subdirectory);
        Path fileA = Files.createFile(subdirectory.resolve("fileA.tif"));
        Path fileB = Files.createFile(subdirectory.resolve("fileB.tif"));
        Path fileC = Files.createFile(subdirectory.resolve("fileC.jpg"));

        List<Path> matched = PathPatternResolver.resolveOne(
            new PathPattern(String.format("%s%s*directory**", tempDir, DIR_SEPERATOR), tempDir)
        );

        assertTrue(matched.contains(fileA));
        assertTrue(matched.contains(fileB));
        assertTrue(matched.contains(fileC));
        assertTrue(matched.contains(subdirectory));
        assertFalse(matched.contains(tempDir));
    }
}
