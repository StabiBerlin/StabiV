package de.spkberlin.imagevalidation.cli.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationValidationConsecutiveNumbering;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
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
        List<String> consistentProperties,
        ConfigurationValidationConsecutiveNumbering consecutiveNumbering,
        Map<Path, ImageValidationResult> imageValidationResults
    ) throws IOException {
        String allImagesValidError = evaluateAllImagesAreValid(imageValidationResults);

        if (Objects.nonNull(allImagesValidError)) {
            logger.info("validation of directory '{}' failed due to '{}'", directory, allImagesValidError);
            return new DirectoryValidationResult(directory, imageValidationResults, allImagesValidError);
        }

        String allPropertiesConsistentError = evaluateAllPropertiesAreConsistent(consistentProperties, imageValidationResults);

        if (Objects.nonNull(allPropertiesConsistentError)) {
            logger.info("validation of directory '{}' failed due to '{}'", directory, allPropertiesConsistentError);
            return new DirectoryValidationResult(directory, imageValidationResults, allPropertiesConsistentError);
        }

        String consecutivenessError = DirectoryConsecutivenessValidation.evaluateConsecutiveness(
            consecutiveNumbering,
            imageValidationResults.keySet()
        );
        if (Objects.nonNull(consecutivenessError)) {
            logger.info("validation of directory '{}' failed due to '{}'", directory, consecutivenessError);
            return new DirectoryValidationResult(directory, imageValidationResults, consecutivenessError);
        }

        logger.info("validation of directory '{}' was successful", directory);
        return new DirectoryValidationResult(directory, imageValidationResults, null);
    }

    /**
     * Evaluate whether properties have consistent values for each image in a directory.
     *
     * @param consistentProperties the properties to check for consistency
     * @param imageValidationResults the image validation results for each image of a directory
     * @return an error message in case properties are inconsistent, otherwise null
     * @throws IOException in case an error occurrs during validation
     */
    public static String evaluateAllPropertiesAreConsistent(
        List<String> consistentProperties,
        Map<Path, ImageValidationResult> imageValidationResults
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        for (String consistentProperty : consistentProperties) {
            Set<String> allValues = new HashSet<>();
            for (Entry<Path, ImageValidationResult> entry : imageValidationResults.entrySet()) {
                Path filepath = entry.getKey();
                ImageValidationResult result = entry.getValue();
                try {
                    Object extractedProperty = JsonPath.read(result.getExtractionResult().getProperties(), consistentProperty);
                    allValues.add(objectMapper.writeValueAsString(extractedProperty));
                } catch (PathNotFoundException e) {
                    logger.error("path '{}' not found in extracted properties", consistentProperty);
                    return String.format("consistent property '%s' not found in image '%s'", consistentProperty, filepath);
                }
            }

            if (allValues.size() != 1) {
                return String.format("property '%s' is not consistent (has %d different values)", consistentProperty, allValues.size());
            }
        }

        return null;
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
}
