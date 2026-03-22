package de.spkberlin.imagevalidation.cli.validation;

public class DirectoryValidationResult {

    private Boolean passed;

    public DirectoryValidationResult(Boolean passed) {
        this.passed = passed;
    }

    public Boolean getPassed() {
        return this.passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
}
