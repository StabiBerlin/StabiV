package de.spkberlin.imagevalidation.common.rules.files.state;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(FileRuleSimpleCondition.class),
        @JsonSubTypes.Type(FileRuleCompositeAndCondition.class),
        @JsonSubTypes.Type(FileRuleCompositeOrCondition.class),
        @JsonSubTypes.Type(FileRuleCompositeNotCondition.class),
    }
)
public abstract class FileRuleCondition {
    //
}
