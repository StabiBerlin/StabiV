package de.spkberlin.stabiv.common.validation.directories.state;

import de.spkberlin.stabiv.common.validation.images.state.ImageValidationResult;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.Map;

public class DirectoryValidationResult {

    private Path directory;
    private Map<Path, ImageValidationResult> imageResults;
    private ValidationResult directoryResult;
    private Integer expectedNumberOfFiles;

    /**
     * Initialize a directory validation result.
     *
     * @param directory the path to the directory
     * @param imageResults all image validation results of all image files inside this directory
     * @param directoryResult validation errors and warnings for this directory
     * @param expectedNumberOfFiles the expected number of files (in case of the consecutive numbering check) or null
     *                              (if there is no expectation)
     */
    public DirectoryValidationResult(
        Path directory,
        Map<Path, ImageValidationResult> imageResults,
        ValidationResult directoryResult,
        Integer expectedNumberOfFiles
    ) {
        this.directory = directory;
        this.imageResults = imageResults;
        this.directoryResult = directoryResult;
        this.expectedNumberOfFiles = expectedNumberOfFiles;
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

    public Integer getExpectedNumberOfFiles() {
        return this.expectedNumberOfFiles;
    }
}
