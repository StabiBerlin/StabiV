package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ValidateAllResult {

    private Map<Path, List<Path>> filesByDirectory;
    private Map<Path, ImageValidationResult> imageValidationResults;

    public ValidateAllResult(Map<Path, List<Path>> filesByDirectory, Map<Path, ImageValidationResult> imageValidationResults) {
        this.filesByDirectory = filesByDirectory;
        this.imageValidationResults = imageValidationResults;
    }

    public Map<Path, List<Path>> getFilesByDirectory() {
        return filesByDirectory;
    }

    public Map<Path, ImageValidationResult> getImageValidationResults() {
        return imageValidationResults;
    }
}
