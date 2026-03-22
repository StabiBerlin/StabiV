package de.spkberlin.imagevalidation.common.extract;

import java.util.Map;

public class ExtractionResult {

    private Map<String, Object> properties;

    public ExtractionResult(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
