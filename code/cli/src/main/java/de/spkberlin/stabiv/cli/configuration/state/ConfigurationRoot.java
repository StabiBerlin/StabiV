package de.spkberlin.stabiv.cli.configuration.state;

import jakarta.validation.Valid;

public class ConfigurationRoot {

    @Valid
    private ConfigurationInputSection input;

    @Valid
    private ConfigurationProcessingSection processing;

    @Valid
    private ConfigurationExtractionSection extraction;

    @Valid
    private ConfigurationValidationSection validation;

    @Valid
    private ConfigurationOutputSection output;

    /**
     * Initialize YamlConfiguration.
     */
    public ConfigurationRoot() {
        input = new ConfigurationInputSection();
        extraction = new ConfigurationExtractionSection();
        validation = new ConfigurationValidationSection();
        output = new ConfigurationOutputSection();
    }

    public ConfigurationInputSection getInput() {
        return input;
    }

    public void setInput(ConfigurationInputSection input) {
        this.input = input;
    }

    public ConfigurationProcessingSection getProcessing() {
        return processing;
    }

    public void setProcessing(ConfigurationProcessingSection processing) {
        this.processing = processing;
    }

    public ConfigurationExtractionSection getExtraction() {
        return extraction;
    }

    public void setExtraction(ConfigurationExtractionSection extraction) {
        this.extraction = extraction;
    }

    public ConfigurationValidationSection getValidation() {
        return validation;
    }

    public void setValidation(ConfigurationValidationSection validation) {
        this.validation = validation;
    }

    public ConfigurationOutputSection getOutput() {
        return output;
    }

    public void setOutput(ConfigurationOutputSection output) {
        this.output = output;
    }
}
