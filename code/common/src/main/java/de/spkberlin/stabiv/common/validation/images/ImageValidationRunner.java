package de.spkberlin.stabiv.common.validation.images;

import de.spkberlin.stabiv.common.checksum.ChecksumUtil;
import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.extract.Extractor;
import de.spkberlin.stabiv.common.extract.ExtractorImplementationsInterface;
import de.spkberlin.stabiv.common.validation.images.state.ImageRule;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageValidationRunner implements Callable<ImageValidationRunnerResult> {

    private static final Logger logger = LoggerFactory.getLogger(ImageValidationRunner.class);

    private Path filepath;
    private ExtractorImplementationsInterface extractors;
    private List<ImageRule> rules;
    private String checksumAlgorithm;

    /**
     * Initialize image validation task for a specific image file.
     *
     * @param filepath the filepath to the image file to be validated
     * @param extractors the extractor modules to use for validation
     * @param rules the image rules to check for validation
     */
    public ImageValidationRunner(
        Path filepath,
        ExtractorImplementationsInterface extractors,
        List<ImageRule> rules,
        String checksumAlgorithm
    ) {
        this.filepath = filepath;
        this.extractors = extractors;
        this.rules = rules;
        this.checksumAlgorithm = checksumAlgorithm;
    }

    @Override
    public ImageValidationRunnerResult call() throws Exception {
        logger.trace("start validating image at {}", filepath);
        ExtractionResult extractionResult = extractImageProperties(filepath, extractors);
        try {
            List<ValidationResult> imageRuleEvaluationResults = evaluateImageRules(filepath, extractionResult, rules);
            try {
                String checksum = ChecksumUtil.calculate(filepath, checksumAlgorithm);
                return new ImageValidationRunnerResult(filepath, extractionResult, imageRuleEvaluationResults, checksum);
            } catch (Exception e) {
                logger.error("calculation of checksum failed with exception", e);
                return new ImageValidationRunnerResult(filepath, extractionResult, imageRuleEvaluationResults, null);
            }
        } catch (Exception e) {
            logger.error("evaluation of images rules failed with exception", e);
            return new ImageValidationRunnerResult(
                filepath,
                extractionResult,
                Collections.singletonList(ValidationResult.fromError("exception during evaluation of image properties")),
                null
            );
        }
    }

    /**
     * Extract image properties using a list of extractors for a single image file.
     *
     * @param filepath the filepath to the image file
     * @param extractors the list of extractor modules to use
     * @return the extracted image properties
     */
    public static ExtractionResult extractImageProperties(Path filepath, ExtractorImplementationsInterface extractors) {
        Map<String, ExtractionResult> resultsByExtractor = new HashMap<>();
        for (Entry<String, Extractor> entry : extractors.getImplementations().entrySet()) {
            try {
                logger.debug("apply extractor {}", entry.getKey());
                resultsByExtractor.put(entry.getKey(), entry.getValue().extractProperties(filepath));
            } catch (Exception e) {
                logger.error(String.format("extractor '%s' failed with exception", entry.getKey()), e);
            }
        }
        return mergeExtractionResults(resultsByExtractor);
    }

    /**
     * Merge extracted properties for all extractors by appending the extractor name to the object tree.
     *
     * @param results extracted properties for each extractor individually
     * @return combined property tree, with root map based on extractor names
     */
    public static ExtractionResult mergeExtractionResults(Map<String, ExtractionResult> results) {
        Map<String, Object> mergedProperties = new HashMap<>();
        for (Entry<String, ExtractionResult> entry : results.entrySet()) {
            if (Objects.nonNull(entry.getValue())) {
                mergedProperties.put(entry.getKey(), entry.getValue().getProperties());
            } else {
                logger.error("extractor '%s' returned 'null' as extraction result", entry.getKey());
            }
        }
        return new ExtractionResult(mergedProperties);
    }

    /**
     * Check a list of image rules against extracted properties for a single image file.
     *
     * @param filepath the filepath to the image file
     * @param extractionResult already extracted properties from all extractors combined
     * @param rules the list of image validation rules to check
     * @return the list of validation results for each rule
     * @throws IOException in case something fails during rule evaluation
     */
    public static List<ValidationResult> evaluateImageRules(Path filepath, ExtractionResult extractionResult, List<ImageRule> rules)
        throws IOException {
        List<ValidationResult> rulesResultList = ImageRuleEvaluator.evaluateRules(rules, extractionResult.getProperties());

        ValidationResult combinedResult = ValidationResult.of(rulesResultList);
        logger.info(
            "evaluation of image rules for file '{}' {} {}{}",
            filepath,
            combinedResult.getErrors().isEmpty() ? "was successfull" : "failed",
            combinedResult.getErrors().size() > 0 ? String.format("with error '%s'", combinedResult.getErrors().get(0)) : "",
            combinedResult.getErrors().isEmpty() && combinedResult.getWarnings().size() > 0
                ? String.format("with warning '%s'", combinedResult.getWarnings().get(0))
                : ""
        );

        return rulesResultList;
    }
}
