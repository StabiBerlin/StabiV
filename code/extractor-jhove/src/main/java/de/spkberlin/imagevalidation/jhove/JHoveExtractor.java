package de.spkberlin.imagevalidation.jhove;

import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import java.nio.file.Path;
import java.util.Map;

public class JHoveExtractor implements Extractor {

    public ExtractionResult extractProperties(Path filename) {
        return new ExtractionResult(Map.of("test", "test"));
    }
}
