package de.spkberlin.imagevalidation.common.validation.directories;

import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.extract.ExtractorImplementationsInterface;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsecutiveNumberingCondition;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsecutiveNumberingResult;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsistencyCondition;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryValidationResult;
import de.spkberlin.imagevalidation.common.validation.images.ImageValidation;
import de.spkberlin.imagevalidation.common.validation.images.ImageValidationRunner;
import de.spkberlin.imagevalidation.common.validation.images.ImageValidationRunnerResult;
import de.spkberlin.imagevalidation.common.validation.images.state.ImageRule;
import de.spkberlin.imagevalidation.common.validation.images.state.ImageValidationResult;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryValidation.class);

    /**
     * Validate all files of a single directory given a full configuration and an already initialized thread pool.
     *
     * @param directory the directory to be validated
     * @param filepaths the image files inside this directory to be validated
     * @param pool an already initialized thread pool
     * @return validation results for the directory and all images
     * @throws ExecutionException in case there is an error while executing a validation task
     * @throws InterruptedException in case execution is interrupted
     */
    public static DirectoryValidationResult validate(
        Path directory,
        List<Path> filepaths,
        ExtractorImplementationsInterface extractors,
        List<ImageRule> imageRules,
        List<DirectoryConsistencyCondition> consistentProperties,
        DirectoryConsecutiveNumberingCondition consecutiveNumbering,
        List<DirectoryPropertyDeviationsRule> propertyDeviations,
        String checksumAlgorithm,
        ExecutorService pool
    ) throws IOException, ExecutionException, InterruptedException {
        logger.info("process directory '{}'", directory);
        // extract properties and evaluate image rules
        Map<Path, ImageValidationRunnerResult> imageRunnerResults = extractPropertiesAndEvaluateImageRules(
            filepaths,
            pool,
            extractors,
            imageRules,
            checksumAlgorithm
        );
        Map<Path, ExtractionResult> extractionResults = mapToExtractionResults(imageRunnerResults);
        Map<Path, String> checksums = mapToChecksums(imageRunnerResults);
        Map<Path, List<ValidationResult>> imageRuleResults = mapToImageRuleResults(imageRunnerResults);

        // check consistent properties
        Map<Path, List<ValidationResult>> consistentPropertyResults = DirectoryConsistencyValidation.evaluateAllPropertiesAreConsistent(
            consistentProperties,
            extractionResults
        );

        // check consecutiveness
        DirectoryConsecutiveNumberingResult consecutivenessResult = DirectoryConsecutivenessValidation.evaluateConsecutiveness(
            consecutiveNumbering,
            filepaths
        );

        // check property deveiations
        Map<Path, List<ValidationResult>> propertyDeviationsResults = DirectoryPropertyDeviationsValidation.evaluatePropertyDeviations(
            propertyDeviations,
            extractionResults
        );

        // combine results for each image
        Map<Path, ImageValidationResult> imageValidationResults = combineImageValidationResults(
            filepaths,
            extractionResults,
            imageRuleResults,
            consistentPropertyResults,
            consecutivenessResult,
            propertyDeviationsResults,
            checksums
        );

        // combine into one directory result
        return new DirectoryValidationResult(
            directory,
            imageValidationResults,
            summarizeDirectoryValidationResult(imageValidationResults, consecutivenessResult)
        );
    }

    /**
     * Combine all individual image-based validation results for all images into a single image validation result object for each image.
     *
     * @param filepaths the image filepaths
     * @param extractionResults the extracted properties for each image
     * @param imageRuleEvaluationResults the image rule evaluation results for each image
     * @param consistentPropertyEvaluationResults validation results for each image checking properties for consistency
     * @param consecutivenessResult validation results for each image checking consecutive numbering within a directory
     * @param propertyDeviationsResults validation results for each image checking property deviations
     * @param checksums checksum for each image
     * @return the combined image validation result object for each image
     */
    public static Map<Path, ImageValidationResult> combineImageValidationResults(
        List<Path> filepaths,
        Map<Path, ExtractionResult> extractionResults,
        Map<Path, List<ValidationResult>> imageRuleEvaluationResults,
        Map<Path, List<ValidationResult>> consistentPropertyEvaluationResults,
        DirectoryConsecutiveNumberingResult consecutivenessResult,
        Map<Path, List<ValidationResult>> propertyDeviationsResults,
        Map<Path, String> checksums
    ) {
        return filepaths
            .stream()
            .collect(
                Collectors.toMap(
                    filepath -> filepath,
                    filepath ->
                        new ImageValidationResult(
                            filepath,
                            extractionResults.get(filepath),
                            imageRuleEvaluationResults.get(filepath),
                            propertyDeviationsResults.get(filepath),
                            consistentPropertyEvaluationResults.get(filepath),
                            consecutivenessResult.getImageResults().get(filepath),
                            checksums.get(filepath)
                        )
                )
            );
    }

    /**
     * Build a summarized validation result for a directory.
     *
     * @param imageValidationResults individual image validation results for all images of the directory
     * @param consecutivenessResult validation result of the consecutiveness check
     * @return a combined validation result for the directory
     */
    public static ValidationResult summarizeDirectoryValidationResult(
        Map<Path, ImageValidationResult> imageValidationResults,
        DirectoryConsecutiveNumberingResult consecutivenessResult
    ) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (imageValidationResults.isEmpty()) {
            errors.add(String.format("directory does not contain any images"));
        }

        if (!imageValidationResults.values().stream().allMatch(ImageValidation::isImageValid)) {
            errors.add(String.format("at least one image failed validation"));
        }

        if (
            imageValidationResults
                .values()
                .stream()
                .map(ImageValidation::getAllWarnings)
                .anyMatch(w -> !w.isEmpty())
        ) {
            warnings.add(String.format("at least one image has a warning"));
        }

        if (!consecutivenessResult.getDirectoryResult().getErrors().isEmpty()) {
            errors.addAll(consecutivenessResult.getDirectoryResult().getErrors());
            warnings.addAll(consecutivenessResult.getDirectoryResult().getWarnings());
        }

        return ValidationResult.of(errors, warnings);
    }

    /**
     * Extract image properties and evaluate all image rules for all images of a directory concurrently.
     *
     * @param filepaths the filepaths for all images of the directory
     * @param pool the executor pool
     * @param extractors the extractors that will be applied
     * @param imageRules the image rules that will be evaluated
     * @param checksumAlgorithm the checksum algorithm that is used to calculate the checksum for each file
     * @return all validation results for all images of the directory
     * @throws InterruptedException in case processing is interrupted
     * @throws ExecutionException in case an error during processing is detected
     */
    public static Map<Path, ImageValidationRunnerResult> extractPropertiesAndEvaluateImageRules(
        List<Path> filepaths,
        ExecutorService pool,
        ExtractorImplementationsInterface extractors,
        List<ImageRule> imageRules,
        String checksumAlgorithm
    ) throws InterruptedException, ExecutionException {
        Map<Path, ImageValidationRunnerResult> imageValidationRunnerResults = new HashMap<>();
        List<Future<ImageValidationRunnerResult>> resultFutures = pool.invokeAll(
            createImageValidationTasks(filepaths, extractors, imageRules, checksumAlgorithm)
        );

        for (Future<ImageValidationRunnerResult> resultFuture : resultFutures) {
            ImageValidationRunnerResult result = resultFuture.get();

            Path filepath = result.getFilePath();
            imageValidationRunnerResults.put(filepath, result);
        }

        return imageValidationRunnerResults;
    }

    /**
     * Create image validation tasks to be run in parallel from a pool of processing threads.
     *
     * @param filepaths the filepaths of images to be validated
     * @param extractors all available extractors
     * @param rules image rules that need to be evaluated
     * @param checksumAlgorithm the checksum algorithm to use
     * @return a list of image validation tasks to be queued for execution
     */
    public static List<ImageValidationRunner> createImageValidationTasks(
        List<Path> filepaths,
        ExtractorImplementationsInterface extractors,
        List<ImageRule> rules,
        String checksumAlgorithm
    ) {
        return filepaths
            .stream()
            .map(filepath -> new ImageValidationRunner(filepath, extractors, rules, checksumAlgorithm))
            .toList();
    }

    /**
     * Return all validation results for a directory as a list.
     *
     * @param directoryResult the directory result containing all validation results
     * @return the list of all validation results
     */
    public static List<ValidationResult> getAllValidationResults(DirectoryValidationResult directoryResult) {
        return Stream.of(
            Collections.singletonList(directoryResult.getDirectoryResult()),
            directoryResult.getImageResults().values().stream().map(ImageValidation::getAllValidationResults).flatMap(List::stream).toList()
        )
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Return whether the directory is valid based on a directory validation result.
     *
     * @param result the directory validation result
     * @return true if the directory is considered valid
     */
    public static boolean isDirectoryValid(DirectoryValidationResult result) {
        return Objects.isNull(getFirstError(result));
    }

    /**
     * Return all errors for the full directory.
     *
     * @param result the directory validation result object
     * @return all errors
     */
    public static List<String> getAllErrors(DirectoryValidationResult result) {
        return getAllValidationResults(result)
            .stream()
            .map(v -> v.getErrors())
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Return all warnings for the full directory.
     *
     * @param result the directory validation result object
     * @return all warnings
     */
    public static List<String> getAllWarnings(DirectoryValidationResult result) {
        return getAllValidationResults(result)
            .stream()
            .map(v -> v.getWarnings())
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Return the first error for a directory validation result.
     *
     * @param result the directory validation result
     * @return the first error or null if none
     */
    public static String getFirstError(DirectoryValidationResult result) {
        return getAllErrors(result).stream().findFirst().orElse(null);
    }

    /**
     * Return the first warning for a directory validation result.
     *
     * @param result the directory validation result
     * @return the first warning or null if none
     */
    public static String getFirstWarning(DirectoryValidationResult result) {
        return getAllWarnings(result).stream().findFirst().orElse(null);
    }

    /**
     * Return the number of valid images in a directory.
     *
     * @param result the directory validation result
     * @return the number of valid images
     */
    public static Long countValidImageForDirectory(DirectoryValidationResult result) {
        return result.getImageResults().values().stream().filter(ImageValidation::isImageValid).count();
    }

    private static Map<Path, ExtractionResult> mapToExtractionResults(Map<Path, ImageValidationRunnerResult> imageRunnerResults) {
        return imageRunnerResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getExtractionResult()));
    }

    private static Map<Path, String> mapToChecksums(Map<Path, ImageValidationRunnerResult> imageRunnerResults) {
        return imageRunnerResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getChecksum()));
    }

    private static Map<Path, List<ValidationResult>> mapToImageRuleResults(Map<Path, ImageValidationRunnerResult> imageRunnerResults) {
        return imageRunnerResults
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getImageRuleEvaluationResults()));
    }
}
