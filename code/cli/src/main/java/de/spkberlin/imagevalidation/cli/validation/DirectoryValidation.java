package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryValidation.class);

    /**
     * Validate a single directory given the validation results of all image files contained in it.
     *
     * @param directory the directory to validate
     * @param imageValidationResults the image validation results for all files contained in it
     * @return the validation result for the directory
     */
    public static DirectoryValidationResult validate(Path directory, Map<Path, ImageValidationResult> imageValidationResults) {
        Boolean directoryValid = imageValidationResults
            .values()
            .stream()
            .allMatch(r -> r.getImageRuleEvaluationResult().getErrors().isEmpty());

        logger.info("validation of directory '{}' {}", directory, directoryValid ? "was successful" : "failed");

        return new DirectoryValidationResult(directory, imageValidationResults, directoryValid);
    }
}
