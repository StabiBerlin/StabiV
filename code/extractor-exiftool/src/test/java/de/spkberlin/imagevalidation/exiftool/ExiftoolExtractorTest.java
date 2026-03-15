package de.spkberlin.imagevalidation.exiftool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit test for simple App. */
public class ExiftoolExtractorTest {

    /** Rigorous Test :-) */
    @Test
    public void testTransformPropertyName() {
        assertEquals(
            ExiftoolExtractor.transformPropertyName("[TIFF-IFD0-ICC_Profile-Header] ProfileVersion"),
            "TIFF/IFD0/ICC_Profile/Header/ProfileVersion"
        );
    }
}
