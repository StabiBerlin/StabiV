package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DirectoryValidationResult {

    private Path directory;
    private Map<Path, ImageValidationResult> imageResults;
    private DirectoryPropertyDeviationsResult propertyDeviationsResult;
    private List<String> errors;
    private List<String> warnings;

    /**
     * Initialize a directory validation result.
     *
     * @param directory the path to the directory
     * @param imageResults all image validation results of all image files inside this directory
     */
    public DirectoryValidationResult(Path directory, Map<Path, ImageValidationResult> imageResults) {
        this.directory = directory;
        this.imageResults = imageResults;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public Path getDirectory() {
        return this.directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return this.warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Map<Path, ImageValidationResult> getImageResults() {
        return this.imageResults;
    }

    public void setImageResults(Map<Path, ImageValidationResult> imageResults) {
        this.imageResults = imageResults;
    }

    public DirectoryPropertyDeviationsResult getPropertyDeviationsResult() {
        return this.propertyDeviationsResult;
    }

    public void setPropertyDeviationsResult(DirectoryPropertyDeviationsResult propertyDeviationsResult) {
        this.propertyDeviationsResult = propertyDeviationsResult;
    }
}
