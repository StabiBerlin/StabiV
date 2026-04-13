package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.cli.extraction.Extraction;
import de.spkberlin.imagevalidation.cli.extraction.ExtractorType;
import de.spkberlin.imagevalidation.common.checksum.ChecksumUtil;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.rules.images.ImageRuleEvaluationResult;
import de.spkberlin.imagevalidation.common.rules.images.ImageRuleEvaluator;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageValidationRunner implements Callable<ImageValidationResult> {

    private static final Logger logger = LoggerFactory.getLogger(ImageValidationRunner.class);

    private Path filepath;
    private List<ExtractorType> extractors;
    private List<ImageRule> rules;
    private String checksumAlgorithm;

    /**
     * Initialize image validation task for a specific image file.
     *
     * @param filepath the filepath to the image file to be validated
     * @param extractors the extractor modules to use for validation
     * @param rules the image rules to check for validation
     */
    public ImageValidationRunner(Path filepath, List<ExtractorType> extractors, List<ImageRule> rules, String checksumAlgorithm) {
        this.filepath = filepath;
        this.extractors = extractors;
        this.rules = rules;
        this.checksumAlgorithm = checksumAlgorithm;
    }

    @Override
    public ImageValidationResult call() throws Exception {
        ExtractionResult extractionResult = extractImageProperties(filepath, extractors);
        ImageRuleEvaluationResult imageValidationResult = evaluateImageRules(filepath, extractionResult, rules);
        String checksum = ChecksumUtil.calculate(filepath, checksumAlgorithm);
        return new ImageValidationResult(filepath, imageValidationResult, extractionResult, checksum);
    }

    /**
     * Extract image properties using a list of extractors for a single image file.
     *
     * @param filepath the filepath to the image file
     * @param extractors the list of extractor modules to use
     * @return the extracted image properties
     */
    public static ExtractionResult extractImageProperties(Path filepath, List<ExtractorType> extractors) {
        Map<ExtractorType, ExtractionResult> resultsByExtractor = new HashMap<>();
        for (ExtractorType extratorType : extractors) {
            logger.debug("apply extractor {}", extratorType);
            resultsByExtractor.put(extratorType, Extraction.getImplementation(extratorType).extractProperties(filepath));
        }

        return mergeExtractionResults(resultsByExtractor);
    }

    /**
     * Merge extracted properties for all extractors by appending the extractor name to the object tree.
     *
     * @param results extracted properties for each extractor individually
     * @return combined property tree, with root map based on extractor names
     */
    public static ExtractionResult mergeExtractionResults(Map<ExtractorType, ExtractionResult> results) {
        Map<String, Object> mergedProperties = new HashMap<>();
        for (Entry<ExtractorType, ExtractionResult> entry : results.entrySet()) {
            mergedProperties.put(entry.getKey().name(), entry.getValue().getProperties());
        }
        return new ExtractionResult(mergedProperties);
    }

    /**
     * Check a list of image rules against extracted properties for a single image file.
     *
     * @param filepath the filepath to the image file
     * @param extractionResult already extracted properties from all extractors combined
     * @param rules the list of image validation rules to check
     * @return a result object containing potential errors or warnings
     * @throws IOException in case something fails during rule evaluation
     */
    public static ImageRuleEvaluationResult evaluateImageRules(Path filepath, ExtractionResult extractionResult, List<ImageRule> rules)
        throws IOException {
        ImageRuleEvaluationResult result = ImageRuleEvaluator.evaluateRules(rules, extractionResult.getProperties());

        logger.info(
            "evaluation of image rules for file '{}' {} {}{}",
            filepath,
            result.getErrors().isEmpty() ? "was successfull" : "failed",
            result.getErrors().size() > 0 ? String.format("with error '%s'", result.getErrors().get(0)) : "",
            result.getErrors().isEmpty() && result.getWarnings().size() > 0
                ? String.format("with warning '%s'", result.getWarnings().get(0))
                : ""
        );

        return result;
    }
}
