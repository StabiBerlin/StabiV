package de.spkberlin.imagevalidation.common.rules;

public class Rule {

    private String context;
    private RuleCondition condition;
    private RuleSeverityType severity;
    private String message;

    public Rule() {
        //
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public RuleCondition getCondition() {
        return this.condition;
    }

    public void setCondition(RuleCondition condition) {
        this.condition = condition;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RuleSeverityType getSeverity() {
        return this.severity;
    }

    public void setSeverity(RuleSeverityType severity) {
        this.severity = severity;
    }
}
