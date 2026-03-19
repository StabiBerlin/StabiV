package de.spkberlin.imagevalidation.cli.configuration.yaml;

public class YamlConfiguration {

    private YamlInputSection input;
    private YamlExtractionSection extraction;
    private YamlValidationSection validation;
    private YamlOutputSection output;

    /**
     * Initialize YamlConfiguration.
     */
    public YamlConfiguration() {
        input = new YamlInputSection();
        extraction = new YamlExtractionSection();
        validation = new YamlValidationSection();
        output = new YamlOutputSection();
    }

    public YamlInputSection getInput() {
        return input;
    }

    public void setInput(YamlInputSection input) {
        this.input = input;
    }

    public YamlExtractionSection getExtraction() {
        return extraction;
    }

    public void setExtraction(YamlExtractionSection extraction) {
        this.extraction = extraction;
    }

    public YamlValidationSection getValidation() {
        return validation;
    }

    public void setValidation(YamlValidationSection validation) {
        this.validation = validation;
    }

    public YamlOutputSection getOutput() {
        return output;
    }

    public void setOutput(YamlOutputSection output) {
        this.output = output;
    }
}
