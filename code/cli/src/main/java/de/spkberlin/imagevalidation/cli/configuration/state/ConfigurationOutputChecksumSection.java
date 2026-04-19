package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.constraints.NotBlank;

public class ConfigurationOutputChecksumSection {

    private String filename;

    @NotBlank
    private String lineFormat;

    public ConfigurationOutputChecksumSection() {
        this.lineFormat = "${checksum} *${filename}";
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLineFormat() {
        return this.lineFormat;
    }

    public void setLineFormat(String lineFormat) {
        this.lineFormat = lineFormat;
    }
}
