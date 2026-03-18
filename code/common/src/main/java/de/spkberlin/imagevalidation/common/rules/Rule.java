package de.spkberlin.imagevalidation.common.rules;

public class Rule {

    private String context;
    private RuleCondition condition;
    private String warning;
    private String error;

    public Rule() {}

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

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getWarning() {
        return this.warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
