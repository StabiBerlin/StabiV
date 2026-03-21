package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import java.util.List;

public class ConfigurationExtractionSection {

    private List<ExtractorType> modules;

    public ConfigurationExtractionSection() {
        modules = List.of(ExtractorType.exiftool, ExtractorType.jhove);
    }

    public List<ExtractorType> getModules() {
        return this.modules;
    }

    public void setModules(List<ExtractorType> modules) {
        this.modules = modules;
    }
}
