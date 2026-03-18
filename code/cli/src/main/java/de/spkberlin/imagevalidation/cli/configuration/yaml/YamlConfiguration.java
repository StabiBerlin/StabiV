package de.spkberlin.imagevalidation.cli.configuration.yaml;

public class YamlConfiguration {

    private YamlInputSection input;
    private YamlValidationSection validation;

    public YamlConfiguration() {}

    public YamlInputSection getInput() {
        return input;
    }

    public void setInput(YamlInputSection input) {
        this.input = input;
    }

    public YamlValidationSection getValidation() {
        return validation;
    }

    public void setValidation(YamlValidationSection validation) {
        this.validation = validation;
    }
}
