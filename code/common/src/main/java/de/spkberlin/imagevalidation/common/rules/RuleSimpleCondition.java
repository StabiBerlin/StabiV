package de.spkberlin.imagevalidation.common.rules;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RuleSimpleCondition extends RuleCondition {

    private String property;
    private RuleSimpleConditionOperatorType operator;

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

    @JsonProperty("assert")
    public RuleSimpleConditionOperatorType getOperator() {
        return operator;
    }

    @JsonProperty("assert")
    public void setOperator(RuleSimpleConditionOperatorType operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
