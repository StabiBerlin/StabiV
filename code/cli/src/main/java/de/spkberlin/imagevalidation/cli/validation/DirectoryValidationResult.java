package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;

public class DirectoryValidationResult {

    private Path directory;
    private Boolean passed;

    public DirectoryValidationResult(Path directory, Boolean passed) {
        this.directory = directory;
        this.passed = passed;
    }

    public Path getDirectory() {
        return this.directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public Boolean getPassed() {
        return this.passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
}
