package de.spkberlin.imagevalidation.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.spkberlin.imagevalidation.common.extract.ExtractionError;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class JHoveExtractorTest {

    public static final Path EXAMPLES_PATH = Path.of("../common/src/test/resources/examples");
    private static final Path SIMPLE_TIF_PATH = EXAMPLES_PATH.resolve("simple/wellformed/simple_rgb.tif");
    private static final Path UNKNOWN_TIF_PATH = EXAMPLES_PATH.resolve("unknown.tif");

    /**
     * Check that some properties are extracted from an example file.
     *
     * @throws ExtractionError in case extraction fails
     */
    @Test
    public void testExampleFile() throws ExtractionError {
        JHoveExtractor extractor = new JHoveExtractor();
        ExtractionResult result = extractor.extractProperties(SIMPLE_TIF_PATH);
        assertFalse(result.getProperties().isEmpty());
    }

    /**
     * Check that extractor raises exception for file that does not exist.
     */
    @Test
    public void testUnknownFile() throws ExtractionError {
        JHoveExtractor extractor = new JHoveExtractor();
        assertThrows(ExtractionError.class, () -> extractor.extractProperties(UNKNOWN_TIF_PATH));
    }
}
