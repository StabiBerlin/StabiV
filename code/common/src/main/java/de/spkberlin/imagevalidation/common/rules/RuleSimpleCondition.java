package de.spkberlin.imagevalidation.common.rules;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RuleSimpleCondition extends RuleCondition {

    private String property;
    private RuleSimpleConditionOperator operator;

    @JsonProperty("value")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> values;

    public RuleSimpleCondition() {
        // empty
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public RuleSimpleConditionOperator getOperator() {
        return operator;
    }

    public void setOperator(RuleSimpleConditionOperator operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
