package de.spkberlin.imagevalidation.cli.extraction;

import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.util.HashMap;
import java.util.Map;

public class Extraction {

    private static final Map<ExtractorType, Extractor> EXTRACTOR_IMPLEMENTATIONS = new HashMap<>();

    public static Extractor getImplementation(ExtractorType extratorType) {
        if (!EXTRACTOR_IMPLEMENTATIONS.containsKey(extratorType)) {
            if (extratorType.equals(ExtractorType.jhove)) {
                EXTRACTOR_IMPLEMENTATIONS.put(ExtractorType.jhove, new JHoveExtractor());
            }
            if (extratorType.equals(ExtractorType.exiftool)) {
                EXTRACTOR_IMPLEMENTATIONS.put(ExtractorType.exiftool, new ExiftoolExtractor());
            }
        }

        return EXTRACTOR_IMPLEMENTATIONS.get(extratorType);
    }
}
