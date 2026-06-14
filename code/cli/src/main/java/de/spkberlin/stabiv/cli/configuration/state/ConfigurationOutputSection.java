package de.spkberlin.stabiv.cli.configuration.state;

import jakarta.validation.Valid;
import java.nio.file.Path;

public class ConfigurationOutputSection {

    private Path baseDirectory;

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

    public Path getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(Path basepath) {
        this.baseDirectory = basepath;
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
