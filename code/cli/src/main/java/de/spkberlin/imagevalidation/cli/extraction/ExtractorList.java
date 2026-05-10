package de.spkberlin.imagevalidation.cli.extraction;

import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.common.extract.ExtractorImplementationsInterface;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractorList implements ExtractorImplementationsInterface {

    private static final Map<ExtractorType, ThreadLocal<Extractor>> EXTRACTOR_IMPLEMENTATIONS = new HashMap<>();

    private List<ExtractorType> activeExtractorTypes;

    public ExtractorList(List<ExtractorType> activeExtractorTypes) {
        this.activeExtractorTypes = activeExtractorTypes;
        initializeExtactor();
    }

    private void initializeExtactor() {
        for (ExtractorType extractorType : activeExtractorTypes) {
            if (!EXTRACTOR_IMPLEMENTATIONS.containsKey(extractorType)) {
                if (extractorType.equals(ExtractorType.jhove)) {
                    EXTRACTOR_IMPLEMENTATIONS.put(ExtractorType.jhove, ThreadLocal.withInitial(() -> new JHoveExtractor()));
                }
                if (extractorType.equals(ExtractorType.exiftool)) {
                    EXTRACTOR_IMPLEMENTATIONS.put(ExtractorType.exiftool, ThreadLocal.withInitial(() -> new ExiftoolExtractor()));
                }
            }
        }
    }

    /**
     * Return a thread-safe implementation of all extractor that are supposed to be applied.
     *
     * @return an initialized thread-safe extractor implementation
     */
    public Map<String, Extractor> getImplementations() {
        return activeExtractorTypes.stream().collect(Collectors.toMap(t -> t.name(), t -> EXTRACTOR_IMPLEMENTATIONS.get(t).get()));
    }
}
