package de.spkberlin.stabiv.cli.configuration.state;

import de.spkberlin.stabiv.cli.extraction.ExtractorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ConfigurationExtractionSection {

    @NotEmpty
    private List<@Valid ExtractorType> modules;

    @Valid
    private ConfigurationExtractionExiftoolSection exiftool;

    public ConfigurationExtractionSection() {
        this.modules = List.of(ExtractorType.exiftool, ExtractorType.jhove);
        this.exiftool = new ConfigurationExtractionExiftoolSection();
    }

    public List<ExtractorType> getModules() {
        return this.modules;
    }

    public void setModules(List<ExtractorType> modules) {
        this.modules = modules;
    }

    public ConfigurationExtractionExiftoolSection getExiftool() {
        return this.exiftool;
    }

    public void setExiftool(ConfigurationExtractionExiftoolSection exiftool) {
        this.exiftool = exiftool;
    }
}
