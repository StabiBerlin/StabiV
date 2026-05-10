package de.spkberlin.imagevalidation.common.validation.images;

import de.spkberlin.imagevalidation.common.validation.images.state.ImageValidationResult;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageValidation {

    private static final Logger logger = LoggerFactory.getLogger(ImageValidation.class);

    /**
     * Return true if an image has passed all checks according to an image validation result.
     * @param result the image validation result
     * @return true if all checks passed
     */
    public static boolean isImageValid(ImageValidationResult result) {
        if (
            Objects.isNull(result) ||
            Objects.isNull(result.getExtractionResult()) ||
            Objects.isNull(result.getExtractionResult().getProperties())
        ) {
            // image validation result misses info
            logger.error("image validation result is null or contains null values");
            return false;
        }
        if (
            result
                .getImageRuleEvaluationResults()
                .stream()
                .anyMatch(r -> !r.getErrors().isEmpty())
        ) {
            // an image validation rule failed
            return false;
        }
        if (
            result
                .getConsistentPropertyEvaluationResults()
                .stream()
                .anyMatch(r -> !r.getErrors().isEmpty())
        ) {
            // an consitent property rule failed
            return false;
        }
        if (!result.getConsecutiveNumberingEvaluationResult().getErrors().isEmpty()) {
            // an consecutive number error occurred
            return false;
        }
        if (
            result
                .getPropertyDeviationEvaluationResults()
                .stream()
                .anyMatch(r -> !r.getErrors().isEmpty())
        ) {
            // a property deviation rule failed
            return false;
        }
        return true;
    }

    /**
     * Return all validation results for a single image.
     *
     * @param result the image validation result object
     * @return the list of all validation results for this image
     */
    public static List<ValidationResult> getAllValidationResults(ImageValidationResult result) {
        return Stream.of(
            result.getImageRuleEvaluationResults(),
            result.getConsistentPropertyEvaluationResults(),
            Collections.singletonList(result.getConsecutiveNumberingEvaluationResult()),
            result.getPropertyDeviationEvaluationResults()
        )
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Return all errors for the image.
     *
     * @param result the image validation result object
     * @return all errors for the image
     */
    public static List<String> getAllErrors(ImageValidationResult result) {
        return getAllValidationResults(result)
            .stream()
            .map(v -> v.getErrors())
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Return all warnings for the image.
     *
     * @param result the image validation result object
     * @return all warnings for the image
     */
    public static List<String> getAllWarnings(ImageValidationResult result) {
        return getAllValidationResults(result)
            .stream()
            .map(v -> v.getWarnings())
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Return the first error for the image.
     *
     * @param result the image validation result object
     * @return the first error that is found
     */
    public static String getFirstError(ImageValidationResult result) {
        return getAllErrors(result).stream().findFirst().orElse(null);
    }

    /**
     * Return the first warning for the image.
     *
     * @param result the image validation result object
     * @return the first warning that is found
     */
    public static String getFirstWarning(ImageValidationResult result) {
        return getAllWarnings(result).stream().findFirst().orElse(null);
    }
}
