package de.spkberlin.stabiv.cli.extraction;

import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExtractionExiftoolSection;
import de.spkberlin.stabiv.common.extract.Extractor;
import de.spkberlin.stabiv.common.extract.ExtractorImplementationsInterface;
import de.spkberlin.stabiv.exiftool.ExiftoolExtractor;
import de.spkberlin.stabiv.jhove.JHoveExtractor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractorList implements ExtractorImplementationsInterface {

    private static final Map<ExtractorType, ThreadLocal<Extractor>> EXTRACTOR_IMPLEMENTATIONS = new HashMap<>();

    private List<ExtractorType> activeExtractorTypes;

    public ExtractorList(List<ExtractorType> activeExtractorTypes, ConfigurationExtractionExiftoolSection exiftoolConfig) {
        this.activeExtractorTypes = activeExtractorTypes;
        initializeExtactor(exiftoolConfig);
    }

    private void initializeExtactor(ConfigurationExtractionExiftoolSection exiftoolConfig) {
        for (ExtractorType extractorType : activeExtractorTypes) {
            if (!EXTRACTOR_IMPLEMENTATIONS.containsKey(extractorType)) {
                if (extractorType.equals(ExtractorType.jhove)) {
                    EXTRACTOR_IMPLEMENTATIONS.put(ExtractorType.jhove, ThreadLocal.withInitial(() -> new JHoveExtractor()));
                }
                if (extractorType.equals(ExtractorType.exiftool)) {
                    EXTRACTOR_IMPLEMENTATIONS.put(
                        ExtractorType.exiftool,
                        ThreadLocal.withInitial(() -> new ExiftoolExtractor(exiftoolConfig.getPath(), exiftoolConfig.getArguments()))
                    );
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
