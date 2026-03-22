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

    public ImageValidationRunner(Path filepath, List<ExtractorType> extractors, List<ImageRule> rules) {
        this.filepath = filepath;
        this.extractors = extractors;
        this.rules = rules;
    }

    @Override
    public ImageValidationResult call() throws Exception {
        ExtractionResult extractionResult = extractImageProperties(filepath, extractors);
        ImageRuleEvaluationResult imageValidationResult = evaluateImageRules(filepath, extractionResult, rules);
        String checksum = ChecksumUtil.calculate(filepath, "MD5");
        return new ImageValidationResult(filepath, imageValidationResult, extractionResult, checksum);
    }

    public static ExtractionResult extractImageProperties(Path filepath, List<ExtractorType> extractors) {
        Map<ExtractorType, ExtractionResult> resultsByExtractor = new HashMap<>();
        for (ExtractorType extratorType : extractors) {
            logger.debug("apply extractor {}", extratorType);
            resultsByExtractor.put(extratorType, Extraction.getImplementation(extratorType).extractProperties(filepath));
        }

        return mergeExtractionResults(resultsByExtractor);
    }

    public static ExtractionResult mergeExtractionResults(Map<ExtractorType, ExtractionResult> results) {
        Map<String, Object> mergedProperties = new HashMap<>();
        for (Entry<ExtractorType, ExtractionResult> entry : results.entrySet()) {
            mergedProperties.put(entry.getKey().name(), entry.getValue().getProperties());
        }
        return new ExtractionResult(mergedProperties);
    }

    public static ImageRuleEvaluationResult evaluateImageRules(Path filepath, ExtractionResult extractionResult, List<ImageRule> rules)
        throws IOException {
        ImageRuleEvaluationResult result = ImageRuleEvaluator.evaluateRules(rules, extractionResult.getProperties());

        logger.info(
            "evaluation of image rules for file '{}' {} {}{}",
            filepath,
            result.getErrors().isEmpty() ? "was successfull" : "failed",
            result.getErrors().size() > 0 ? String.format("with error '%s'", result.getErrors().get(0)) : "",
            result.getWarnings().size() > 0 ? String.format("with warning '%s'", result.getWarnings().get(0)) : ""
        );

        return result;
    }
}
