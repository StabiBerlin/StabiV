package de.spkberlin.imagevalidation.common.validation.directories;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsistencyCondition;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryConsistencyValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryConsistencyValidation.class);

    /**
     * Evaluate whether properties have consistent values for each image in a directory.
     *
     * @param consistencyConditions the properties to check for consistency
     * @param extractionResults the extracted properties of all files of a directory
     * @return an error message in case properties are inconsistent, otherwise null
     * @throws IOException in case an error occurrs during validation
     */
    public static Map<Path, List<ValidationResult>> evaluateAllPropertiesAreConsistent(
        List<DirectoryConsistencyCondition> consistencyConditions,
        Map<Path, ExtractionResult> extractionResults
    ) throws IOException {
        return consistencyConditions
            .stream()
            .map(consistencyCondition -> {
                Map<Path, Optional<String>> extractedValues = extractPropertyForAllImages(
                    extractionResults,
                    consistencyCondition.getProperty()
                );
                Optional<String> mostFrequentValue = findMostFrequentValue(extractedValues.values());
                return extractionResults
                    .entrySet()
                    .stream()
                    .collect(
                        Collectors.toMap(Map.Entry::getKey, entry -> {
                            Path filepath = entry.getKey();
                            if (extractedValues.get(filepath).isEmpty()) {
                                return ValidationResult.fromError(
                                    String.format("could not extract property '%s'", consistencyCondition.getLabel())
                                );
                            } else if (mostFrequentValue.isEmpty()) {
                                return ValidationResult.fromError(
                                    String.format("could not determine consistent value for property '%s'", consistencyCondition.getLabel())
                                );
                            } else if (!mostFrequentValue.get().equals(extractedValues.get(filepath).get())) {
                                return ValidationResult.fromError(
                                    String.format(
                                        "value '%s' for property '%s' is not consistent",
                                        extractedValues.get(filepath).get(),
                                        consistencyCondition.getLabel()
                                    )
                                );
                            }
                            return ValidationResult.fromSuccess();
                        })
                    );
            })
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    /**
     * Extract a specific property for all images to be checked for consistency.
     *
     * @param extractionResults the extraction results for all images
     * @param property the property to be extracted
     * @return the extracted value for each image
     */
    public static Map<Path, Optional<String>> extractPropertyForAllImages(Map<Path, ExtractionResult> extractionResults, String property) {
        // ObjectMapper objectMapper = new ObjectMapper();
        return extractionResults
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, entry -> {
                    Path filepath = entry.getKey();
                    ExtractionResult result = entry.getValue();
                    if (Objects.isNull(result)) {
                        logger.trace("no extracted properties for image '%s'", filepath);
                        return Optional.empty();
                    }
                    try {
                        Object extractedProperty = JsonPath.read(result.getProperties(), property);
                        return Optional.of(PropertiesSerializer.toPrettyString(extractedProperty));
                    } catch (PathNotFoundException e) {
                        logger.trace("path '{}' not found in extracted properties", property);
                        return Optional.empty();
                    } /* catch (IOException e) {
                        logger.trace("exception extracting property '{}' during consistency check: {}", property, e);
                        return Optional.empty();
                    }*/
                })
            );
    }

    private static Optional<String> findMostFrequentValue(Collection<Optional<String>> extractedValues) {
        Map<String, Long> extractedValuesCounts = extractedValues
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return extractedValuesCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }
}
