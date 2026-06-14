package de.spkberlin.stabiv.cli.configuration.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

public class ConfigurationExcelStyleSection {

    @Valid
    @JsonProperty("default")
    private ConfigurationExcelStyleElement defaultElement;

    @Valid
    private ConfigurationExcelStyleElement header;

    @Valid
    private ConfigurationExcelStyleElement error;

    @Valid
    private ConfigurationExcelStyleElement warning;

    /**
     * Initialize style section with default values.
     */
    public ConfigurationExcelStyleSection() {
        this.defaultElement = new ConfigurationExcelStyleElement(false, null, "#888888");
        this.header = new ConfigurationExcelStyleElement(true, "#dddddd", "#888888");
        this.error = new ConfigurationExcelStyleElement(false, "#ff7d55", "#9d2609");
        this.warning = new ConfigurationExcelStyleElement(false, "#ffff00", "#888800");
    }

    public ConfigurationExcelStyleElement getDefaultElement() {
        return this.defaultElement;
    }

    public void setDefaultElement(ConfigurationExcelStyleElement defaultElement) {
        this.defaultElement = defaultElement;
    }

    public ConfigurationExcelStyleElement getHeader() {
        return this.header;
    }

    public void setHeader(ConfigurationExcelStyleElement header) {
        this.header = header;
    }

    public ConfigurationExcelStyleElement getError() {
        return this.error;
    }

    public void setError(ConfigurationExcelStyleElement error) {
        this.error = error;
    }

    public ConfigurationExcelStyleElement getWarning() {
        return this.warning;
    }

    public void setWarning(ConfigurationExcelStyleElement warning) {
        this.warning = warning;
    }
}
