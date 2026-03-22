package de.spkberlin.imagevalidation.common.rules.images;

import java.util.List;

public class ImageRuleEvaluationResult {

    private List<String> errors;
    private List<String> warnings;

    public ImageRuleEvaluationResult(List<String> errors, List<String> warnings) {
        this.errors = errors;
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return this.warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
