package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationFailureSeverity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DirectoryConsistencyCondition {

    @NotBlank
    private String label;

    @NotBlank
    private String property;

    @NotNull
    private Boolean reportColumn;

    @Valid
    private ValidationFailureSeverity severity;

    /**
     * Initialize condition with default values.
     */
    public DirectoryConsistencyCondition() {
        this.label = null;
        this.property = null;
        this.reportColumn = true;
        this.severity = ValidationFailureSeverity.error;
    }

    /**
     * Initialize a consistency condition.
     *
     * @param label the label of the property to check for consistency (used to generate nice error messages)
     * @param property the JSONPath of the property to check for consistency
     * @param severity the severity in case the property is not consistent
     * @param reportColumn whether the property should be added to the validation report
     */
    public DirectoryConsistencyCondition(String label, String property, ValidationFailureSeverity severity, Boolean reportColumn) {
        this.label = label;
        this.property = property;
        this.reportColumn = reportColumn;
        this.severity = severity;
    }

    public DirectoryConsistencyCondition(String label, String property) {
        this(label, property, ValidationFailureSeverity.error, true);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public ValidationFailureSeverity getSeverity() {
        return this.severity;
    }

    public void setSeverity(ValidationFailureSeverity severity) {
        this.severity = severity;
    }

    public Boolean isReportColumn() {
        return this.reportColumn;
    }

    public void setReportColumn(Boolean reportColumn) {
        this.reportColumn = reportColumn;
    }
}
