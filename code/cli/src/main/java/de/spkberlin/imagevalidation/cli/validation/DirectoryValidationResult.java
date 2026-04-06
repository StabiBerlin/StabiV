package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;
import java.util.Map;

public class DirectoryValidationResult {

    private Path directory;
    private Boolean passed;
    private Map<Path, ImageValidationResult> imageResults;

    /**
     * Initialize a directory validation result.
     *
     * @param directory the path to the directory
     * @param imageResults all image validation results of all image files inside this directory
     * @param passed whether the directory passed validation
     */
    public DirectoryValidationResult(Path directory, Map<Path, ImageValidationResult> imageResults, Boolean passed) {
        this.directory = directory;
        this.imageResults = imageResults;
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

    public Map<Path, ImageValidationResult> getImageResults() {
        return this.imageResults;
    }

    public void setImageResults(Map<Path, ImageValidationResult> imageResults) {
        this.imageResults = imageResults;
    }
}
