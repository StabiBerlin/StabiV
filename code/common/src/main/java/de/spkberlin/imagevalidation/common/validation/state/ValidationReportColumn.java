package de.spkberlin.imagevalidation.common.validation.state;

import jakarta.validation.constraints.NotBlank;

public class ValidationReportColumn {

    @NotBlank
    private String label;

    @NotBlank
    private String property;

    public ValidationReportColumn() {
        // empty
    }

    public ValidationReportColumn(String label, String path) {
        this.label = label;
        this.property = path;
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

    public void setProperty(String path) {
        this.property = path;
    }
}
