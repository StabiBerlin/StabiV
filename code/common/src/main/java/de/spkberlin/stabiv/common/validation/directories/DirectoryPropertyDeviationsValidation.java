package de.spkberlin.stabiv.common.validation.directories;

import com.jayway.jsonpath.JsonPath;
import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsAggregateType;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsSliceEntry;
import de.spkberlin.stabiv.common.validation.state.ValidationFailureSeverity;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryPropertyDeviationsValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryPropertyDeviationsValidation.class);

    /**
     * Check all property deviation rules given extracted properties for images of a directory.
     *
     * @param deviationEntries the property deviation rules
     * @param extractionResults extracted properties for images of a directory
     * @return an error message, or null if successful
     */
    public static Map<Path, List<ValidationResult>> evaluatePropertyDeviations(
        Collection<DirectoryPropertyDeviationsRule> deviationEntries,
        Map<Path, ExtractionResult> extractionResults
    ) {
        if (deviationEntries.isEmpty()) {
            return extractionResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> Collections.emptyList()));
        }

        return deviationEntries
            .stream()
            .map(entry -> evaluatePropertyDeviationEntry(entry, extractionResults))
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    /**
     * Check a single property deviation rule given extracted properties for images of a directory.
     *
     * @param deviationEntry the property deviation rule
     * @param extractionResults extracted propeties for images of a directory
     * @return an error message, or null if successful
     */
    public static Map<Path, ValidationResult> evaluatePropertyDeviationEntry(
        DirectoryPropertyDeviationsRule deviationEntry,
        Map<Path, ExtractionResult> extractionResults
    ) {
        Map<Path, Optional<Double>> propertyValues = findNumberProperties(deviationEntry.getProperty(), extractionResults);

        Set<Path> checkedImages = new HashSet<>();
        Map<Path, ValidationResult> slicesResults = checkPropertyDeviationsAdditionalSlices(
            propertyValues,
            deviationEntry.getAdditionalSlices(),
            deviationEntry.getLabel(),
            checkedImages
        );

        Map<Path, ValidationResult> otherResults = checkDefaultPropertyDeviations(
            propertyValues,
            checkedImages,
            deviationEntry.getCondition(),
            deviationEntry.getLabel()
        );

        return Stream.of(slicesResults, otherResults)
            .flatMap(m -> m.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (keyA, keyB) -> keyA));
    }

    /**
     * Calculate the aggregate of a set of number values.
     *
     * @param values the collection of numbers that need to be aggregated
     * @param aggregateType the aggregation type (e.g. average or median)
     * @return the aggregate of all numbers
     */
    public static Double calculateNumberAggregate(
        Collection<Optional<Double>> values,
        DirectoryPropertyDeviationsAggregateType aggregateType
    ) {
        if (DirectoryPropertyDeviationsAggregateType.average.equals(aggregateType)) {
            return values.stream().filter(Optional::isPresent).map(Optional::get).mapToDouble(Double::doubleValue).average().orElse(0.0);
        } else if (DirectoryPropertyDeviationsAggregateType.median.equals(aggregateType)) {
            List<Double> sorted = values.stream().filter(Optional::isPresent).map(Optional::get).sorted().toList();
            if (sorted.size() == 0) {
                return 0.0;
            }
            return sorted.get(sorted.size() / 2);
        }
        throw new RuntimeException(String.format("unknown aggregate type '%s'", aggregateType.name()));
    }

    /**
     * Check that property values do not deviate more than a maximum allowed deviation from their aggregate for all images of a slice.
     *
     * @param propertyValues the property values to check for each image of a slice
     * @param aggregateType the aggregation type (e.g. average or median)
     * @param maxDeviation the maximum allowed deviation
     * @param inPercent whether the maximum allowed deviation should be interpreted as percent
     * @param propertyLabel the label of the property that is checked
     * @return an result object (with error or warning message) for each image of the slice
     */
    public static Map<Path, ValidationResult> checkMaxDeviationForSlice(
        Map<Path, Optional<Double>> propertyValues,
        DirectoryPropertyDeviationsAggregateType aggregateType,
        Double maxDeviation,
        Boolean inPercent,
        String propertyLabel,
        ValidationFailureSeverity severity
    ) {
        double aggregate = calculateNumberAggregate(propertyValues.values(), aggregateType);
        return propertyValues
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, e ->
                    checkMaxDeviationsForImage(
                        e.getKey(),
                        aggregate,
                        e.getValue(),
                        aggregateType,
                        maxDeviation,
                        inPercent,
                        propertyLabel,
                        severity
                    )
                )
            );
    }

    /**
     * Check that property value do not deviate more than a maximum allowed deviation from their aggregate for a single image.
     *
     * @param imagePath the path to the image file
     * @param aggregate the aggregate value to compare to
     * @param value the extracted value for the image
     * @param aggregateType the aggregation type (e.g. average or median)
     * @param maxDeviation the maximum allowed deviation
     * @param inPercent whether the maximum allowed deviation should be interpreted as percent
     * @param propertyLabel the label of the property that is checked
     * @return an result object with an error or warning message for the image
     */
    public static ValidationResult checkMaxDeviationsForImage(
        Path imagePath,
        Double aggregate,
        Optional<Double> value,
        DirectoryPropertyDeviationsAggregateType aggregateType,
        Double maxDeviation,
        Boolean inPercent,
        String propertyLabel,
        ValidationFailureSeverity severity
    ) {
        if (!value.isPresent() || Objects.isNull(value.get())) {
            return ValidationResult.of(
                String.format("could not extract property '%s' when checking maximum deviation", propertyLabel),
                severity
            );
        }
        if (inPercent) {
            double deviationInPercent = (Math.abs(value.get() - aggregate) / aggregate) * 100.0;
            if (deviationInPercent > maxDeviation) {
                return ValidationResult.of(
                    String.format(
                        "property '%s' deviates by %.1f percent from %s of %.1f",
                        propertyLabel,
                        deviationInPercent,
                        aggregateType.name(),
                        aggregate
                    ),
                    severity
                );
            }
        } else {
            double absoluteDeviation = Math.abs(value.get() - aggregate);
            if (absoluteDeviation > maxDeviation) {
                return ValidationResult.of(
                    String.format(
                        "property '%s' deviates by %.1f from %s of %.1f",
                        propertyLabel,
                        absoluteDeviation,
                        aggregateType.name(),
                        aggregate
                    ),
                    severity
                );
            }
        }

        return ValidationResult.fromSuccess();
    }

    /**
     * Filter the map of property values to images that match the specified slice parameters.
     *
     * <p>The slice is calculated by ordering images lexicographically by their filepath, meaning start index 1 refers to the first image
     * in the directory. In reversed mode, start index 1 refers to the last image in the directory.
     *
     * @param propertyValues the map of property values
     * @param from the starting index (inclusive)
     * @param to the end index (inclusive)
     * @param reversed if false, indexes are interpreted from the beginning of the file list, otherwise from the back
     * @return property values for images filtered by slice parameters
     */
    public static Map<Path, Optional<Double>> getRelevantValuesForSlice(
        Map<Path, Optional<Double>> propertyValues,
        Integer from,
        Integer to,
        Boolean reversed
    ) {
        if (propertyValues.isEmpty()) {
            return propertyValues;
        }

        List<Path> orderedPaths = new ArrayList<>(propertyValues.keySet());
        Collections.sort(orderedPaths);

        if (reversed) {
            Collections.reverse(orderedPaths);
        }

        Set<Path> relevantPaths = new HashSet<>(
            orderedPaths.subList(Math.min(orderedPaths.size() - 1, from - 1), Math.min(orderedPaths.size() - 1, to))
        );

        return propertyValues
            .entrySet()
            .stream()
            .filter(e -> relevantPaths.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Check property deviations for a single slice.
     *
     * @param propertyValues the property values
     * @param sliceEntry the slice definition
     * @param propertyLabel the label of the property that is being checked
     * @param checkedImages a set of filepaths that is modified to contain images that were checked based on the slice
     * @return an error message, or null if successful
     */
    public static Map<Path, ValidationResult> checkPropertyDeviationsForSlice(
        Map<Path, Optional<Double>> propertyValues,
        DirectoryPropertyDeviationsSliceEntry sliceEntry,
        String propertyLabel,
        Set<Path> checkedImages
    ) {
        Map<Path, Optional<Double>> relevantValues = getRelevantValuesForSlice(
            propertyValues,
            sliceEntry.getFrom(),
            sliceEntry.getTo(),
            sliceEntry.isReversed()
        );

        Map<Path, ValidationResult> results = checkMaxDeviationForSlice(
            relevantValues,
            sliceEntry.getCondition().getAggregate(),
            sliceEntry.getCondition().getMaxDeviation(),
            sliceEntry.getCondition().isInPercent(),
            propertyLabel,
            sliceEntry.getCondition().getSeverity()
        );

        // remember which images have been checked based on sequence entries
        checkedImages.addAll(relevantValues.keySet());

        return results;
    }

    /**
     * Check all additional slices for a property deviation rule.
     *
     * @param propertyValues the property values
     * @param sliceEntries the list of slices
     * @param propertyLabel the label of the property being checked
     * @param checkedImages a set of filepaths that is modified to contain images that were checked based on all slices
     * @return an error message, or null if successful
     */
    public static Map<Path, ValidationResult> checkPropertyDeviationsAdditionalSlices(
        Map<Path, Optional<Double>> propertyValues,
        List<DirectoryPropertyDeviationsSliceEntry> sliceEntries,
        String propertyLabel,
        Set<Path> checkedImages
    ) {
        return sliceEntries
            .stream()
            .map(sliceEntry -> checkPropertyDeviationsForSlice(propertyValues, sliceEntry, propertyLabel, checkedImages))
            .flatMap(results -> results.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (keyA, keyB) -> keyA));
    }

    /**
     * Check remaining image files for property deviations.
     *
     * @param propertyValues the property values
     * @param alreadyCheckedImages the set of images that were already checked by slice definitions
     * @param condition the property deviation condition
     * @param propertyLabel the label of the property being checked
     * @return an error message, or null if successful
     */
    public static Map<Path, ValidationResult> checkDefaultPropertyDeviations(
        Map<Path, Optional<Double>> propertyValues,
        Set<Path> alreadyCheckedImages,
        DirectoryPropertyDeviationsCondition condition,
        String propertyLabel
    ) {
        Map<Path, Optional<Double>> relevantValues = propertyValues
            .entrySet()
            .stream()
            .filter(e -> !alreadyCheckedImages.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Path, ValidationResult> results = checkMaxDeviationForSlice(
            relevantValues,
            condition.getAggregate(),
            condition.getMaxDeviation(),
            condition.isInPercent(),
            propertyLabel,
            condition.getSeverity()
        );

        return results;
    }

    /**
     * Find an integer property for all files given already extracted properties.
     *
     * @param property the property to find
     * @param extractionResults the extraction result for every file
     * @return a map of property values for each file
     */
    public static Map<Path, Optional<Double>> findNumberProperties(String property, Map<Path, ExtractionResult> extractionResults) {
        return extractionResults
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, entry -> {
                    try {
                        return Optional.of(Double.parseDouble(JsonPath.read(entry.getValue().getProperties(), property)));
                    } catch (Exception e) {
                        logger.debug("could not parse number property '{}' when checking property deviations due to '{}'", property, e);
                        return Optional.empty();
                    }
                })
            );
    }
}
