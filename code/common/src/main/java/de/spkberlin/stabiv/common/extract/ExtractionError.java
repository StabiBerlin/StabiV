package de.spkberlin.stabiv.common.extract;

public class ExtractionError extends RuntimeException {

    public ExtractionError(String message) {
        super(message);
    }

    public ExtractionError(Exception e) {
        super(e);
    }
}
