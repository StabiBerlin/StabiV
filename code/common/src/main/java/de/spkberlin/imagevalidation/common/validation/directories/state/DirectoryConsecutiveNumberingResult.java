package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.Map;

public class DirectoryConsecutiveNumberingResult {

    private Map<Path, ValidationResult> imageResults;

    private ValidationResult directoryResult;

    private Integer expectedNumberOfFiles;

    /**
     * Initialize consecutive numbering validation result.
     *
     * @param imageResults the individual validation error or warning messages for each image
     * @param directoryResult error or warning messages for the directory
     * @param expectedNumberOfFiles the expected number of images deduced from the maximum image number
     */
    public DirectoryConsecutiveNumberingResult(
        Map<Path, ValidationResult> imageResults,
        ValidationResult directoryResult,
        Integer expectedNumberOfFiles
    ) {
        this.imageResults = imageResults;
        this.directoryResult = directoryResult;
        this.expectedNumberOfFiles = expectedNumberOfFiles;
    }

    public Map<Path, ValidationResult> getImageResults() {
        return this.imageResults;
    }

    public ValidationResult getDirectoryResult() {
        return this.directoryResult;
    }

    public Integer getExpectedNumberOfFiles() {
        return this.expectedNumberOfFiles;
    }
}
