package de.spkberlin.imagevalidation.cli.configuration.yaml;

import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import java.util.List;

public class YamlExtractionSection {

    private List<ExtractorType> modules;

    public YamlExtractionSection() {
        modules = List.of(ExtractorType.exiftool, ExtractorType.jhove);
    }

    public List<ExtractorType> getModules() {
        return this.modules;
    }

    public void setModules(List<ExtractorType> modules) {
        this.modules = modules;
    }
}
