package de.spkberlin.imagevalidation.common.rules;

public class RuleSimpleCondition extends RuleCondition {

    private String property;
    private String operator;
    private String value;

    public RuleSimpleCondition() {}

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
