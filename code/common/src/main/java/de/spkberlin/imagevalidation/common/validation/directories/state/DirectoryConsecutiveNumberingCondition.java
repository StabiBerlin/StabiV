package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationFailureSeverity;
import jakarta.validation.constraints.Min;

public class DirectoryConsecutiveNumberingCondition {

    private String filenameRegExp;

    private String filenameTemplate;

    private ValidationFailureSeverity severity;

    @Min(0)
    private Integer numberMatchGroup;

    @Min(0)
    private Integer startNumber;

    /**
     * Intialize consecutiveness rule with default values.
     */
    public DirectoryConsecutiveNumberingCondition() {
        this.filenameRegExp = null;
        this.filenameTemplate = null;
        this.numberMatchGroup = 1;
        this.startNumber = 1;
        this.severity = ValidationFailureSeverity.error;
    }

    public DirectoryConsecutiveNumberingCondition(String filenameRegExp) {
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

    public String getFilenameTemplate() {
        return this.filenameTemplate;
    }

    public void setFilenameTemplate(String filenameTemplate) {
        this.filenameTemplate = filenameTemplate;
    }

    public ValidationFailureSeverity getSeverity() {
        return this.severity;
    }

    public void setSeverity(ValidationFailureSeverity severity) {
        this.severity = severity;
    }
}
