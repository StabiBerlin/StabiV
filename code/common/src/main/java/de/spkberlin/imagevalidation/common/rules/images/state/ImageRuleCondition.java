package de.spkberlin.imagevalidation.common.rules.images.state;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(ImageRuleSimpleCondition.class),
        @JsonSubTypes.Type(ImageRuleCompositeAndCondition.class),
        @JsonSubTypes.Type(ImageRuleCompositeOrCondition.class),
        @JsonSubTypes.Type(ImageRuleCompositeNotCondition.class),
    }
)
public abstract class ImageRuleCondition {
    //
}
