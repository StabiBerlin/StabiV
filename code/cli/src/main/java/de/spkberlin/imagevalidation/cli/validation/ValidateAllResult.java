package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ValidateAllResult {

    private Map<Path, List<Path>> filesByDirectory;
    private Map<Path, ImageValidationResult> imageValidationResults;
    private Map<Path, DirectoryValidationResult> directoryValidationResults;

    /**
     * Initialize validate all result.
     *
     * @param filesByDirectory the checked directories and their files
     * @param imageValidationResults a map of image validation results for each file
     * @param directoryValidationResults a map of directory validation results for each directory
     */
    public ValidateAllResult(
        Map<Path, List<Path>> filesByDirectory,
        Map<Path, ImageValidationResult> imageValidationResults,
        Map<Path, DirectoryValidationResult> directoryValidationResults
    ) {
        this.filesByDirectory = filesByDirectory;
        this.imageValidationResults = imageValidationResults;
        this.directoryValidationResults = directoryValidationResults;
    }

    public Map<Path, List<Path>> getFilesByDirectory() {
        return filesByDirectory;
    }

    public Map<Path, ImageValidationResult> getImageValidationResults() {
        return imageValidationResults;
    }

    public Map<Path, DirectoryValidationResult> getDirectoryValidationResults() {
        return directoryValidationResults;
    }
}
