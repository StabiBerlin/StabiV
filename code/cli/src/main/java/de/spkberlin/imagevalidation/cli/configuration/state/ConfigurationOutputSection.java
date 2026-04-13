package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;

public class ConfigurationOutputSection {

    @Valid
    private ConfigurationCsvSection csv;

    @Valid
    private ConfigurationLoggingSection logging;

    public ConfigurationOutputSection() {
        csv = new ConfigurationCsvSection();
        logging = new ConfigurationLoggingSection();
    }

    public ConfigurationCsvSection getCsv() {
        return this.csv;
    }

    public void setCsv(ConfigurationCsvSection csv) {
        this.csv = csv;
    }

    public ConfigurationLoggingSection getLogging() {
        return this.logging;
    }

    public void setLogging(ConfigurationLoggingSection logging) {
        this.logging = logging;
    }
}
