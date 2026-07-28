package de.spkberlin.stabiv.cli.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PathPatternTypeTest {

    @TempDir
    private Path tempDir;

    /**
     * Check that type of pattern is correctly calculated for various example patterns.
     */
    @Test
    public void testPatternType() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.tif"));

        assertEquals(PathPatternType.EXACT, PathPatternType.getType(tempDir.toString(), tempDir));
        assertEquals(PathPatternType.EXACT, PathPatternType.getType(testFile.toString(), tempDir));
        assertEquals(PathPatternType.SIMPLE, PathPatternType.getType("/other/file.tif", tempDir));
        assertEquals(PathPatternType.SIMPLE, PathPatternType.getType("/test/*.tif", tempDir));
        assertEquals(PathPatternType.GLOB, PathPatternType.getType("glob:/test/*.tif", tempDir));
        assertEquals(PathPatternType.REGEX, PathPatternType.getType("regex:^/test/.*\\.tif$", tempDir));
    }
}
