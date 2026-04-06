package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;
import java.util.Map;

public class DirectoryValidationResult {

    private Path directory;
    private Map<Path, ImageValidationResult> imageResults;
    private String error;

    /**
     * Initialize a directory validation result.
     *
     * @param directory the path to the directory
     * @param imageResults all image validation results of all image files inside this directory
     * @param error an error message if a directory is not valid
     */
    public DirectoryValidationResult(Path directory, Map<Path, ImageValidationResult> imageResults, String error) {
        this.directory = directory;
        this.imageResults = imageResults;
        this.error = error;
    }

    public Path getDirectory() {
        return this.directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<Path, ImageValidationResult> getImageResults() {
        return this.imageResults;
    }

    public void setImageResults(Map<Path, ImageValidationResult> imageResults) {
        this.imageResults = imageResults;
    }
}
