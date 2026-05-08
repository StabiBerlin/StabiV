package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationOutputChecksumSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationValidationConsecutiveNumbering;
import de.spkberlin.imagevalidation.cli.output.checksum.ChecksumFileWriter;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.validation.directories.DirectoryConsecutivenessValidation;
import de.spkberlin.imagevalidation.common.validation.directories.DirectoryConsistencyValidation;
import de.spkberlin.imagevalidation.common.validation.directories.DirectoryPropertyDeviationsValidation;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsistencyCondition;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsResult;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import de.spkberlin.imagevalidation.common.validation.state.ValidationSeverityType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryValidation.class);

    /**
     * Validate a single directory given the validation results of all image files contained in it.
     *
     * @param directory the directory to validate
     * @param consistentProperties the list of properties to check for consistency per directory
     * @param imageValidationResults the image validation results for all files contained in it
     * @return the validation result for the directory
     */
    public static DirectoryValidationResult validate(
        Path directory,
        List<DirectoryConsistencyCondition> consistentProperties,
        ConfigurationValidationConsecutiveNumbering consecutiveNumbering,
        List<DirectoryPropertyDeviationsRule> propertyDeviations,
        Map<Path, ImageValidationResult> imageValidationResults,
        ConfigurationOutputChecksumSection checksumConfig
    ) throws IOException {
        DirectoryValidationResult result = new DirectoryValidationResult(directory, imageValidationResults);

        if (imageValidationResults.isEmpty()) {
            logger.info("validation of directory '{}' failed because it does not contain images", directory);
            result.getErrors().add(String.format("directory does not contain any images"));
        }

        String allImagesValidError = evaluateAllImagesAreValid(imageValidationResults);
        if (Objects.nonNull(allImagesValidError)) {
            logger.info("validation of directory '{}' failed due to '{}'", directory, allImagesValidError);
            result.getErrors().add(allImagesValidError);
        }

        checkPropertiesAreConsistent(directory, consistentProperties, result);
        checkConsecutiveness(directory, consecutiveNumbering, result);
        checkPropertyDeviations(directory, propertyDeviations, result);

        if (result.getErrors().isEmpty()) {
            logger.info("validation of directory '{}' was successful", directory);
            ChecksumFileWriter.writeFile(
                directory,
                checksumConfig.getFilename(),
                checksumConfig.getLineFormat(),
                mapToChecksums(imageValidationResults)
            );
        }
        return result;
    }

    private static void checkPropertiesAreConsistent(
        Path directory,
        List<DirectoryConsistencyCondition> consistentProperties,
        DirectoryValidationResult result
    ) throws IOException {
        String allPropertiesConsistentError = DirectoryConsistencyValidation.evaluateAllPropertiesAreConsistent(
            consistentProperties,
            mapToExtractionResults(result.getImageResults())
        );
        if (Objects.nonNull(allPropertiesConsistentError)) {
            logger.info("validation of directory '{}' failed due to '{}'", directory, allPropertiesConsistentError);
            result.getErrors().add(allPropertiesConsistentError);
        }
    }

    private static void checkConsecutiveness(
        Path directory,
        ConfigurationValidationConsecutiveNumbering consecutiveNumbering,
        DirectoryValidationResult result
    ) {
        String consecutivenessError = DirectoryConsecutivenessValidation.evaluateConsecutiveness(
            consecutiveNumbering.getFilenameRegExp(),
            consecutiveNumbering.getNumberMatchGroup(),
            consecutiveNumbering.getStartNumber(),
            result.getImageResults().keySet()
        );
        if (Objects.nonNull(consecutivenessError)) {
            logger.info("validation of directory '{}' failed due to '{}'", directory, consecutivenessError);
            result.getErrors().add(consecutivenessError);
        }
    }

    private static void checkPropertyDeviations(
        Path directory,
        List<DirectoryPropertyDeviationsRule> propertyDeviations,
        DirectoryValidationResult result
    ) {
        DirectoryPropertyDeviationsResult propertyDeviationsResult = DirectoryPropertyDeviationsValidation.evaluatePropertyDeviations(
            propertyDeviations,
            mapToExtractionResults(result.getImageResults())
        );

        String propertyDeviationsWarning = DirectoryPropertyDeviationsValidation.getFirstMessageOfType(
            propertyDeviationsResult,
            ValidationSeverityType.warning
        );
        if (Objects.nonNull(propertyDeviationsWarning)) {
            logger.info("property deviations check for directory '{}' has warning '{}'", directory, propertyDeviationsWarning);
            result.getWarnings().add(propertyDeviationsWarning);
        }

        String propertyDeviationsError = DirectoryPropertyDeviationsValidation.getFirstMessageOfType(
            propertyDeviationsResult,
            ValidationSeverityType.error
        );
        if (Objects.nonNull(propertyDeviationsError)) {
            logger.info("property deviations check for directory '{}' failed due to '{}'", directory, propertyDeviationsError);
            result.getErrors().add(propertyDeviationsError);
        }

        result.setPropertyDeviationsResult(propertyDeviationsResult);
    }

    private static String evaluateAllImagesAreValid(Map<Path, ImageValidationResult> imageValidationResults) {
        Boolean allValid = imageValidationResults
            .values()
            .stream()
            .allMatch(r -> r.getImageRuleEvaluationResult().getErrors().isEmpty());
        if (!allValid) {
            return "at least one image has a validation error";
        }
        return null;
    }

    private static Map<Path, ExtractionResult> mapToExtractionResults(Map<Path, ImageValidationResult> imageValidationResults) {
        return imageValidationResults
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getExtractionResult()));
    }

    private static Map<Path, String> mapToChecksums(Map<Path, ImageValidationResult> imageValidationResults) {
        return imageValidationResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getChecksum()));
    }
}
