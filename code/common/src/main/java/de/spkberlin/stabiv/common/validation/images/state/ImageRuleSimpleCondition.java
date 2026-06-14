package de.spkberlin.stabiv.common.validation.images.state;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ImageRuleSimpleCondition extends ImageRuleCondition {

    @NotEmpty
    private String property;

    @NotNull
    private ImageRuleSimpleConditionOperatorType operator;

    @JsonProperty("value")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> values;

    public ImageRuleSimpleCondition() {
        // empty
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @JsonProperty("assert")
    public ImageRuleSimpleConditionOperatorType getOperator() {
        return operator;
    }

    @JsonProperty("assert")
    public void setOperator(ImageRuleSimpleConditionOperatorType operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
