package de.spkberlin.imagevalidation.common.extract;

import java.nio.file.Path;
import java.util.Map;

public interface Extractor {
    /**
     * Extract a tree of properties from the image file represented as a hierachical object of maps and lists of subproperties or values.
     *
     * <p>Values are encoded a string. The object tree should correspond to a parsed JSON document that can be navigated via JsonPath.</p>
     *
     * @param filepath the filepath of the image file that should be parsed
     * @return the property tree
     * @throws ExtractionError
     */
    public Map<String, Object> extractProperties(Path filepath) throws ExtractionError;
}
