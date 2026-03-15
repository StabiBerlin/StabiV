package de.spkberlin.imagevalidation.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GlobResolverTest {

    @TempDir
    Path tempDir;

    @Test
    public void testPatternAbsolute() {
        // absolute patterns
        assertTrue(GlobResolver.isPatternAbsolute(Path.of("/test/test.tif")));
        assertTrue(GlobResolver.isPatternAbsolute(Path.of("/test/*.tif")));
        assertTrue(GlobResolver.isPatternAbsolute(Path.of("/test/**/original/*.{jpg,tif}")));

        // relative patterns
        assertFalse(GlobResolver.isPatternAbsolute(Path.of("test/test.tif")));
        assertFalse(GlobResolver.isPatternAbsolute(Path.of("./test/*.tif")));
        assertFalse(GlobResolver.isPatternAbsolute(Path.of("../test/**/original/*.{jpg,tif}")));
    }

    @Test
    void testMakePatternAbsolute() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.makePatternAbsolute(Path.of("./test/*.tif"), cwd), Path.of("/tmp/test/*.tif"));
    }

    @Test
    public void testFindPatternBasePath() {
        Path cwd = Path.of("/tmp");
        assertEquals(GlobResolver.findPatternBasePath(Path.of("./test/*.tif"), cwd), Path.of("/tmp/test"));
        assertEquals(GlobResolver.findPatternBasePath(Path.of("/custom/test/*.tif"), cwd), Path.of("/custom/test"));
        assertEquals(GlobResolver.findPatternBasePath(Path.of("/custom/test/123.tif"), cwd), Path.of("/custom/test/123.tif"));
    }

    @Test
    public void testResolveAbsolutePattern() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(tempDir.resolve("*.tif"));

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertFalse(matched.contains(file3));
    }

    @Test
    public void testResolveRelativePattern() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.tif"));
        Path file2 = Files.createFile(tempDir.resolve("file2.tif"));
        Path file3 = Files.createFile(tempDir.resolve("file3.jpg"));

        List<Path> matched = GlobResolver.resolveOne(Path.of("./*.tif"), tempDir);

        assertTrue(matched.contains(file1));
        assertTrue(matched.contains(file2));
        assertFalse(matched.contains(file3));
    }
}
