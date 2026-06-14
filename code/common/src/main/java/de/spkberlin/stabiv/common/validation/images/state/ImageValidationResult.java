package de.spkberlin.stabiv.common.validation.images.state;

import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ImageValidationResult {

    private Path filepath;
    private ExtractionResult extractionResult;
    private List<ValidationResult> imageRuleEvaluationResults;
    private List<ValidationResult> propertyDeviationEvaluationResults;
    private List<ValidationResult> consistentPropertyEvaluationResults;
    private ValidationResult consecutiveNumberingEvaluationResult;
    private String checksum;

    /**
     * Initialize new image validation result.
     *
     * @param filepath the filepath to the image that was validated
     * @param extractionResult the extracted image properties for all extractors
     * @param imageRuleEvaluationResults the list of errors and warnings for each image property rule
     * @param propertyDeviationEvaluationResults the list of errors and warnings for each property deviation rule
     * @param consistentPropertyEvaluationResults the list of errors and warnings for each consisent property rule
     * @param consecutiveNumberingEvaluationResult the list of errors and warnings related to the consecutive numbering check
     * @param checksum the image file checksum
     */
    public ImageValidationResult(
        Path filepath,
        ExtractionResult extractionResult,
        List<ValidationResult> imageRuleEvaluationResults,
        List<ValidationResult> propertyDeviationEvaluationResults,
        List<ValidationResult> consistentPropertyEvaluationResults,
        ValidationResult consecutiveNumberingEvaluationResult,
        String checksum
    ) {
        this.filepath = filepath;
        this.extractionResult = extractionResult;
        this.imageRuleEvaluationResults = Collections.unmodifiableList(imageRuleEvaluationResults);
        this.propertyDeviationEvaluationResults = Collections.unmodifiableList(propertyDeviationEvaluationResults);
        this.consistentPropertyEvaluationResults = Collections.unmodifiableList(consistentPropertyEvaluationResults);
        this.consecutiveNumberingEvaluationResult = consecutiveNumberingEvaluationResult;
        this.checksum = checksum;
    }

    public Path getFilePath() {
        return filepath;
    }

    public List<ValidationResult> getImageRuleEvaluationResults() {
        return imageRuleEvaluationResults;
    }

    public List<ValidationResult> getPropertyDeviationEvaluationResults() {
        return propertyDeviationEvaluationResults;
    }

    public List<ValidationResult> getConsistentPropertyEvaluationResults() {
        return consistentPropertyEvaluationResults;
    }

    public ValidationResult getConsecutiveNumberingEvaluationResult() {
        return consecutiveNumberingEvaluationResult;
    }

    public ExtractionResult getExtractionResult() {
        return extractionResult;
    }

    public String getChecksum() {
        return checksum;
    }
}
