package de.spkberlin.imagevalidation.common.validation.directories;

import com.jayway.jsonpath.JsonPath;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsAggregateType;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsCondition;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsSliceEntry;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DirectoryPropertyDeviationsValidation {

    /**
     * Check all property deviation rules given extracted properties for images of a directory.
     *
     * @param deviationEntries the property deviation rules
     * @param extractionResults extracted properties for images of a directory
     * @return an error message, or null if successful
     */
    public static String evaluatePropertyDeviations(
        Collection<DirectoryPropertyDeviationsRule> deviationEntries,
        Map<Path, ExtractionResult> extractionResults
    ) {
        for (DirectoryPropertyDeviationsRule entry : deviationEntries) {
            String error = evaluatePropertyDeviationEntry(entry, extractionResults);
            if (Objects.nonNull(error)) {
                return error;
            }
        }
        return null;
    }

    /**
     * Check a single property deviation rule given extracted properties for images of a directory.
     *
     * @param deviationEntry the property deviation rule
     * @param extractionResults extracted propeties for images of a directory
     * @return an error message, or null if successful
     */
    public static String evaluatePropertyDeviationEntry(
        DirectoryPropertyDeviationsRule deviationEntry,
        Map<Path, ExtractionResult> extractionResults
    ) {
        Map<Path, Double> propertyValues = findNumberProperties(deviationEntry.getProperty(), extractionResults);

        Set<Path> checkedImages = new HashSet<>();
        String slicesError = checkPropertyDeviationsAdditionalSlices(
            propertyValues,
            deviationEntry.getAdditionalSlices(),
            deviationEntry.getLabel(),
            checkedImages
        );
        if (Objects.nonNull(slicesError)) {
            return slicesError;
        }

        String conditionError = checkDefaultPropertyDeviations(
            propertyValues,
            checkedImages,
            deviationEntry.getCondition(),
            deviationEntry.getLabel()
        );
        if (Objects.nonNull(conditionError)) {
            return conditionError;
        }

        return null;
    }

    /**
     * Calculate the aggregate of a set of number values.
     *
     * @param values the collection of numbers that need to be aggregated
     * @param aggregateType the aggregation type (e.g. average or median)
     * @return the aggregate of all numbers
     */
    public static Double calculateNumberAggregate(Collection<Double> values, DirectoryPropertyDeviationsAggregateType aggregateType) {
        if (DirectoryPropertyDeviationsAggregateType.average.equals(aggregateType)) {
            return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        } else if (DirectoryPropertyDeviationsAggregateType.median.equals(aggregateType)) {
            List<Double> sorted = values.stream().sorted().toList();
            if (sorted.size() == 0) {
                return 0.0;
            }
            return sorted.get(sorted.size() / 2);
        }
        throw new RuntimeException(String.format("unknown aggregate type '%s'", aggregateType.name()));
    }

    /**
     * Check that property values do not deviate more than a maximum allowed deviation from their aggregate.
     *
     * @param propertyValues the property values to check
     * @param aggregateType the aggregation type (e.g. average or median)
     * @param maxDeviation the maximum allowed deviation
     * @param inPercent whether the maximum allowed deviation should be interpreted as percent
     * @param propertyLabel the label of the property that is checked
     * @return an error message, or null if successful
     */
    public static String checkMaxDeviation(
        Map<Path, Double> propertyValues,
        DirectoryPropertyDeviationsAggregateType aggregateType,
        Double maxDeviation,
        Boolean inPercent,
        String propertyLabel
    ) {
        double aggregate = calculateNumberAggregate(propertyValues.values(), aggregateType);
        for (Entry<Path, Double> entry : propertyValues.entrySet()) {
            Path imagePath = entry.getKey();
            Double value = entry.getValue();
            if (inPercent) {
                double deviationInPercent = (Math.abs(value - aggregate) / aggregate) * 100.0;
                if (deviationInPercent > maxDeviation) {
                    return String.format(
                        "image '%s' deviates for property '%s' by %.1f percent from %s of %.1f",
                        imagePath.getFileName(),
                        propertyLabel,
                        deviationInPercent,
                        aggregateType.name(),
                        aggregate
                    );
                }
            } else {
                double absoluteDeviation = Math.abs(value - aggregate);
                if (absoluteDeviation > maxDeviation) {
                    return String.format(
                        "image '%s' deviates for property '%s' by %.1f from %s of %.1f",
                        imagePath.getFileName(),
                        propertyLabel,
                        absoluteDeviation,
                        aggregateType.name(),
                        aggregate
                    );
                }
            }
        }
        return null;
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
    public static Map<Path, Double> getRelevantValuesForSlice(
        Map<Path, Double> propertyValues,
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
    public static String checkPropertyDeviationsForSlice(
        Map<Path, Double> propertyValues,
        DirectoryPropertyDeviationsSliceEntry sliceEntry,
        String propertyLabel,
        Set<Path> checkedImages
    ) {
        Map<Path, Double> relevantValues = getRelevantValuesForSlice(
            propertyValues,
            sliceEntry.getFrom(),
            sliceEntry.getTo(),
            sliceEntry.isReversed()
        );

        String error = checkMaxDeviation(
            relevantValues,
            sliceEntry.getCondition().getAggregate(),
            sliceEntry.getCondition().getMaxDeviation(),
            sliceEntry.getCondition().isInPercent(),
            propertyLabel
        );
        if (Objects.nonNull(error)) {
            return error;
        }

        // remember which images have been checked based on sequence entries
        checkedImages.addAll(relevantValues.keySet());

        return null;
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
    public static String checkPropertyDeviationsAdditionalSlices(
        Map<Path, Double> propertyValues,
        List<DirectoryPropertyDeviationsSliceEntry> sliceEntries,
        String propertyLabel,
        Set<Path> checkedImages
    ) {
        for (DirectoryPropertyDeviationsSliceEntry sliceEntry : sliceEntries) {
            String sliceError = checkPropertyDeviationsForSlice(propertyValues, sliceEntry, propertyLabel, checkedImages);
            if (Objects.nonNull(sliceError)) {
                return sliceError;
            }
        }
        return null;
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
    public static String checkDefaultPropertyDeviations(
        Map<Path, Double> propertyValues,
        Set<Path> alreadyCheckedImages,
        DirectoryPropertyDeviationsCondition condition,
        String propertyLabel
    ) {
        Map<Path, Double> relevantValues = propertyValues
            .entrySet()
            .stream()
            .filter(e -> !alreadyCheckedImages.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String error = checkMaxDeviation(
            relevantValues,
            condition.getAggregate(),
            condition.getMaxDeviation(),
            condition.isInPercent(),
            propertyLabel
        );
        if (Objects.nonNull(error)) {
            return error;
        }

        return null;
    }

    /**
     * Find an integer property for all files given already extracted properties.
     *
     * @param property the property to find
     * @param extractionResults the extraction result for every file
     * @return a map of property values for each file
     */
    public static Map<Path, Double> findNumberProperties(String property, Map<Path, ExtractionResult> extractionResults) {
        return extractionResults
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> Double.parseDouble(JsonPath.read(e.getValue().getProperties(), property))));
    }
}
