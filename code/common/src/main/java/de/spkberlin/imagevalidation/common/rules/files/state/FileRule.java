package de.spkberlin.imagevalidation.common.rules.files.state;

public class FileRule {

    private String context;
    private FileRuleCondition condition;
    private FileRuleSeverityType severity;
    private String message;

    public FileRule() {
        //
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public FileRuleCondition getCondition() {
        return this.condition;
    }

    public void setCondition(FileRuleCondition condition) {
        this.condition = condition;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FileRuleSeverityType getSeverity() {
        return this.severity;
    }

    public void setSeverity(FileRuleSeverityType severity) {
        this.severity = severity;
    }
}
