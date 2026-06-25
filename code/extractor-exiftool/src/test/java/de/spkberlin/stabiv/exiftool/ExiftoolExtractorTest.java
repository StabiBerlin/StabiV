package de.spkberlin.stabiv.exiftool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.spkberlin.stabiv.common.extract.ExtractionError;
import de.spkberlin.stabiv.common.extract.ExtractionResult;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ExiftoolExtractorTest {

    private static final Path EXAMPLE_PATH = Path.of("src/test/resources/examples/simple_rgb.tif");

    /**
     * Check that some properties are extracted from an example file.
     *
     * @throws ExtractionError in case extraction fails
     */
    @Test
    public void testExampleFile() throws ExtractionError {
        ExiftoolExtractor extractor = new ExiftoolExtractor();
        ExtractionResult result = extractor.extractProperties(EXAMPLE_PATH);
        assertFalse(result.getProperties().isEmpty());
    }

    /**
     * Check that an exception is thrown if a file is provided that does not exist.
     */
    @Test
    public void testUnknownFile() {
        ExiftoolExtractor extractor = new ExiftoolExtractor();
        assertThrows(ExtractionError.class, () -> extractor.extractProperties(Path.of("unknown/file.tif")));
    }

    /**
     * Check that an exception is thrown if a file is provided that does not exist.
     */
    @Test
    public void testIncorrectOutputFormat() {
        ExiftoolExtractor extractor = new ExiftoolExtractor(List.of("-X"));
        assertThrows(ExtractionError.class, () -> extractor.extractProperties(EXAMPLE_PATH));
    }

    /**
     * Check that an exception is thrown if a file is provided that does not exist.
     */
    @Test
    public void testIncorrectCommand() {
        ExiftoolExtractor extractor = new ExiftoolExtractor(Path.of("exiftoolSomething"), ExiftoolExtractor.DEFAULT_CLI_ARGS);
        assertThrows(ExtractionError.class, () -> extractor.extractProperties(EXAMPLE_PATH));
    }

    /**
     * Check that single line converted to correct property path.
     **/
    @Test
    public void testTransformPropertyToJsonPath() {
        assertEquals(
            "TIFF/IFD[0]/ICC_Profile/Header/ProfileVersion",
            ExiftoolExtractor.parsePropertyToPath("[TIFF-IFD0-ICC_Profile-Header] ProfileVersion", Set.of("TIFF-IFD"))
        );

        assertEquals(
            "TIFF/IFD0/ICC_Profile/Header2/ProfileVersion4",
            ExiftoolExtractor.parsePropertyToPath("[TIFF-IFD0-ICC_Profile-Header2] ProfileVersion4", Collections.emptySet())
        );
    }

    /**
     * Test that prefixes are correctly extracted for properties.
     */
    @Test
    public void testPropertyPrefixSet() {
        assertEquals(
            Set.of("TIFF-IFD0", "TIFF", "TIFF-IFD0-Metadata2"),
            ExiftoolExtractor.propertyPrefixSet(
                Arrays.asList("[TIFF-IFD0] ImageWidth : 10", "[TIFF-IFD0] ImageHeight : 10", "[TIFF-IFD0-Metadata2] MyMetadata : 123")
            )
        );
    }

    /**
     * Check that array properties are correctly identified.
     */
    @Test
    public void testArrayPropertyPrefixSet() {
        assertEquals(
            Set.of("TIFF-IFD", "TIFF-IFD0-Correct"),
            ExiftoolExtractor.arrayPropertyPrefixSet(
                Set.of(
                    "TIFF-IFD0",
                    "TIFF-IFD0-Metadata2",
                    "TIFF-IFD0-Something",
                    "TIFF-IFD0-Something0",
                    "TIFF-IFD0-Correct0",
                    "TIFF-IFD0-Correct1",
                    "TIFF-IFD0-Gap0",
                    "TIFF-IFD0-Gap2",
                    "TIFF-IFD1"
                )
            )
        );
    }

    /**
     * Check that the version information is returned.
     */
    @Test
    public void testVersion() {
        ExiftoolExtractor extractor = new ExiftoolExtractor();

        assertNotEquals(null, extractor.getVersion());
        assertNotEquals("unknown version", extractor.getVersion());

        extractor = new ExiftoolExtractor(Path.of("exiftool-invalid-command"), Collections.emptyList());
        assertEquals("unknown version", extractor.getVersion());
    }
}
