package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.images.state.ImageValidationResult;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.Map;

public class DirectoryValidationResult {

    private Path directory;
    private Map<Path, ImageValidationResult> imageResults;
    private ValidationResult directoryResult;

    /**
     * Initialize a directory validation result.
     *
     * @param directory the path to the directory
     * @param imageResults all image validation results of all image files inside this directory
     */
    public DirectoryValidationResult(Path directory, Map<Path, ImageValidationResult> imageResults, ValidationResult directoryResult) {
        this.directory = directory;
        this.imageResults = imageResults;
        this.directoryResult = directoryResult;
    }

    public Path getDirectory() {
        return this.directory;
    }

    public ValidationResult getDirectoryResult() {
        return this.directoryResult;
    }

    public Map<Path, ImageValidationResult> getImageResults() {
        return this.imageResults;
    }
}
