package de.spkberlin.imagevalidation.cli.configuration.state;

public class ConfigurationOutputSection {

    private ConfigurationCsvSection csv;
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
