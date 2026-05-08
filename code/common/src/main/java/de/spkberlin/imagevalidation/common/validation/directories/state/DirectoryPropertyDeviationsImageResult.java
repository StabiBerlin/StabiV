package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationSeverityType;

public class DirectoryPropertyDeviationsImageResult {

    String message;

    ValidationSeverityType severity;

    public DirectoryPropertyDeviationsImageResult() {
        this.message = null;
        this.severity = null;
    }

    public DirectoryPropertyDeviationsImageResult(String message, ValidationSeverityType severity) {
        this.message = message;
        this.severity = severity;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ValidationSeverityType getSeverity() {
        return this.severity;
    }

    public void setSeverity(ValidationSeverityType severity) {
        this.severity = severity;
    }
}
