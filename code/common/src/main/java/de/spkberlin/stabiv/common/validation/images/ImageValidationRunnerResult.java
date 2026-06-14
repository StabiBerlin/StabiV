package de.spkberlin.stabiv.common.validation.images;

import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.List;

public class ImageValidationRunnerResult {

    private Path filepath;
    private List<ValidationResult> imageRuleEvaluationResults;
    private ExtractionResult extractionResult;
    private String checksum;

    /**
     * Initialize new image validation result.
     *
     * @param filepath the filepath to the image that was validated
     * @param imageRuleEvaluationResults the validation results for each image rule
     * @param extractionResult the extracted image properties for all extractors
     * @param checksum the image file checksum
     */
    public ImageValidationRunnerResult(
        Path filepath,
        ExtractionResult extractionResult,
        List<ValidationResult> imageRuleEvaluationResults,
        String checksum
    ) {
        this.filepath = filepath;
        this.extractionResult = extractionResult;
        this.imageRuleEvaluationResults = imageRuleEvaluationResults;
        this.checksum = checksum;
    }

    public Path getFilePath() {
        return filepath;
    }

    public List<ValidationResult> getImageRuleEvaluationResults() {
        return imageRuleEvaluationResults;
    }

    public ExtractionResult getExtractionResult() {
        return extractionResult;
    }

    public String getChecksum() {
        return checksum;
    }
}
