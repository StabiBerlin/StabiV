package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.constraints.Min;

public class ConfigurationValidationConsecutiveNumbering {

    private String filenameRegExp;

    @Min(0)
    private Integer numberMatchGroup;

    @Min(0)
    private Integer startNumber;

    public ConfigurationValidationConsecutiveNumbering() {
        this.numberMatchGroup = 1;
        this.startNumber = 1;
    }

    public ConfigurationValidationConsecutiveNumbering(String filenameRegExp) {
        this();
        this.filenameRegExp = filenameRegExp;
    }

    public String getFilenameRegExp() {
        return this.filenameRegExp;
    }

    public void setFilenameRegExp(String filenameRegExp) {
        this.filenameRegExp = filenameRegExp;
    }

    public Integer getNumberMatchGroup() {
        return this.numberMatchGroup;
    }

    public void setNumberMatchGroup(Integer numberMatchGroup) {
        this.numberMatchGroup = numberMatchGroup;
    }

    public Integer getStartNumber() {
        return this.startNumber;
    }

    public void setStartNumber(Integer startNumber) {
        this.startNumber = startNumber;
    }
}
