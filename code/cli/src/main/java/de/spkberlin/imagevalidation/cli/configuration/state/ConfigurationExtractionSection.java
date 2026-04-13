package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.cli.extraction.ExtractorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ConfigurationExtractionSection {

    @NotEmpty
    private List<@Valid ExtractorType> modules;

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
