package de.spkberlin.imagevalidation.common.extract;

import java.nio.file.Path;

public interface Extractor {
    public ExtractionResult extractProperties(Path filename) throws ExtractionError;
}
