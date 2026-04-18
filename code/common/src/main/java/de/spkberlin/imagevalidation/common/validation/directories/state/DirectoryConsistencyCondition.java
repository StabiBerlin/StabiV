package de.spkberlin.imagevalidation.common.validation.directories.state;

import jakarta.validation.constraints.NotBlank;

public class DirectoryConsistencyCondition {

    @NotBlank
    private String label;

    @NotBlank
    private String property;

    public DirectoryConsistencyCondition() {
        // empty
    }

    public DirectoryConsistencyCondition(String label, String property) {
        this.label = label;
        this.property = property;
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
}
