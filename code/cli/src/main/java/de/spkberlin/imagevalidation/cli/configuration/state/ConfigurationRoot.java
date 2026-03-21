package de.spkberlin.imagevalidation.cli.configuration.state;

public class ConfigurationRoot {

    private ConfigurationInputSection input;
    private ConfigurationExtractionSection extraction;
    private ConfigurationValidationSection validation;
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
