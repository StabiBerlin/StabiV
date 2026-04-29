package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;

public class ConfigurationOutputSection {

    @Valid
    private ConfigurationReportSection report;

    @Valid
    private ConfigurationLoggingSection logging;

    @Valid
    private ConfigurationOutputChecksumSection checksum;

    /**
     * Initialize ConfigurationOutputSection.
     */
    public ConfigurationOutputSection() {
        report = new ConfigurationReportSection();
        logging = new ConfigurationLoggingSection();
        checksum = new ConfigurationOutputChecksumSection();
    }

    public ConfigurationReportSection getReport() {
        return this.report;
    }

    public void setReport(ConfigurationReportSection csv) {
        this.report = csv;
    }

    public ConfigurationLoggingSection getLogging() {
        return this.logging;
    }

    public void setLogging(ConfigurationLoggingSection logging) {
        this.logging = logging;
    }

    public ConfigurationOutputChecksumSection getChecksum() {
        return this.checksum;
    }

    public void setChecksum(ConfigurationOutputChecksumSection checksum) {
        this.checksum = checksum;
    }
}
