package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.Map;

public class DirectoryConsecutiveNumberingResult {

    private Map<Path, ValidationResult> imageResults;

    private ValidationResult directoryResult;

    public DirectoryConsecutiveNumberingResult(Map<Path, ValidationResult> imageResults, ValidationResult directoryResult) {
        this.imageResults = imageResults;
        this.directoryResult = directoryResult;
    }

    public Map<Path, ValidationResult> getImageResults() {
        return this.imageResults;
    }

    public ValidationResult getDirectoryResult() {
        return this.directoryResult;
    }
}
