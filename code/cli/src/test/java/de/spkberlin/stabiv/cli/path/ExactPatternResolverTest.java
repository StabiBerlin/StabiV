package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class ExactPatternResolverTest {

    @TempDir
    private Path tempDir;

    /**
     * Check that exact filepaths are correctly resolved.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testExactDirectoryLinux() throws IOException {
        final Path existingFile = Files.createFile(tempDir.resolve("test.tif"));
        final Path missingDirectory = Path.of("/other");
        final Path missingFile = missingDirectory.resolve("test.tif");

        // single existing directory
        assertEquals(tempDir, ExactPatternResolver.getPathMatcher(tempDir.toString(), tempDir).getKey());
        assertTrue(ExactPatternResolver.getPathMatcher(tempDir.toString(), tempDir).getValue().matches(existingFile));

        // single existing file
        assertEquals(tempDir, ExactPatternResolver.getPathMatcher(existingFile.toString(), tempDir).getKey());
        assertTrue(ExactPatternResolver.getPathMatcher(existingFile.toString(), tempDir).getValue().matches(existingFile));

        // missing directory is assumed to be a file
        assertNotEquals(missingDirectory, ExactPatternResolver.getPathMatcher(missingDirectory.toString(), missingDirectory).getKey());
        assertFalse(ExactPatternResolver.getPathMatcher(missingDirectory.toString(), missingDirectory).getValue().matches(missingFile));
        assertTrue(ExactPatternResolver.getPathMatcher(missingDirectory.toString(), missingDirectory).getValue().matches(missingDirectory));
    }

    /**
     * Check that exact filepaths are correctly resolved.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testExactDirectoryWindows() throws IOException {
        final Path existingFile = Files.createFile(tempDir.resolve("test.tif"));
        final Path missingDirectory = Path.of("C:\\other");
        final Path missingFile = missingDirectory.resolve("test.tif");

        // single existing directory
        assertEquals(tempDir, ExactPatternResolver.getPathMatcher(tempDir.toString(), tempDir).getKey());
        assertTrue(ExactPatternResolver.getPathMatcher(tempDir.toString(), tempDir).getValue().matches(existingFile));

        // single existing file
        assertEquals(tempDir, ExactPatternResolver.getPathMatcher(existingFile.toString(), tempDir).getKey());
        assertTrue(ExactPatternResolver.getPathMatcher(existingFile.toString(), tempDir).getValue().matches(existingFile));

        // missing directory is assumed to be a file
        assertNotEquals(missingDirectory, ExactPatternResolver.getPathMatcher(missingDirectory.toString(), missingDirectory).getKey());
        assertFalse(ExactPatternResolver.getPathMatcher(missingDirectory.toString(), missingDirectory).getValue().matches(missingFile));
        assertTrue(ExactPatternResolver.getPathMatcher(missingDirectory.toString(), missingDirectory).getValue().matches(missingDirectory));
    }
}
