package de.spkberlin.imagevalidation.cli.validation;

import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryValidation.class);

    public static DirectoryValidationResult validate(Path directory, Map<Path, ImageValidationResult> imageValidationResults) {
        Boolean directoryValid = imageValidationResults
            .values()
            .stream()
            .allMatch(r -> r.getImageRuleEvaluationResult().getErrors().isEmpty());

        logger.info("validation of directory '{}' {}", directory, directoryValid ? "was successful" : "failed");

        return new DirectoryValidationResult(directoryValid);
    }
}
