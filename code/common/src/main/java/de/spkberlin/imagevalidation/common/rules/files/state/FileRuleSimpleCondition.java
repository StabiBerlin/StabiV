package de.spkberlin.imagevalidation.common.rules.files.state;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FileRuleSimpleCondition extends FileRuleCondition {

    private String property;
    private FileRuleSimpleConditionOperatorType operator;

    @JsonProperty("value")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> values;

    public FileRuleSimpleCondition() {
        // empty
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @JsonProperty("assert")
    public FileRuleSimpleConditionOperatorType getOperator() {
        return operator;
    }

    @JsonProperty("assert")
    public void setOperator(FileRuleSimpleConditionOperatorType operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
