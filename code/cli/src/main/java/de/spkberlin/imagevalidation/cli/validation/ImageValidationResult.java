package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.rules.images.ImageRuleEvaluationResult;
import java.nio.file.Path;

public class ImageValidationResult {

    private Path filepath;
    private ImageRuleEvaluationResult imageRuleEvaluationResult;
    private ExtractionResult extractionResult;
    private String checksum;

    public ImageValidationResult(
        Path filepath,
        ImageRuleEvaluationResult imageRuleEvaluationResult,
        ExtractionResult extractionResult,
        String checksum
    ) {
        this.filepath = filepath;
        this.imageRuleEvaluationResult = imageRuleEvaluationResult;
        this.extractionResult = extractionResult;
        this.checksum = checksum;
    }

    public Path getFilePath() {
        return filepath;
    }

    public ImageRuleEvaluationResult getImageRuleEvaluationResult() {
        return imageRuleEvaluationResult;
    }

    public ExtractionResult getExtractionResult() {
        return extractionResult;
    }

    public String getChecksum() {
        return checksum;
    }
}
