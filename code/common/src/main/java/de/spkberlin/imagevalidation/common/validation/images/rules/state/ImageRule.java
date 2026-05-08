package de.spkberlin.imagevalidation.common.validation.images.rules.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationSeverityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class ImageRule {

    private String context;

    @Valid
    private ImageRuleCondition condition;

    @Valid
    private ValidationSeverityType severity;

    @NotBlank
    private String message;

    public ImageRule() {
        //
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public ImageRuleCondition getCondition() {
        return this.condition;
    }

    public void setCondition(ImageRuleCondition condition) {
        this.condition = condition;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ValidationSeverityType getSeverity() {
        return this.severity;
    }

    public void setSeverity(ValidationSeverityType severity) {
        this.severity = severity;
    }
}
