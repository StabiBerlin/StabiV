package de.spkberlin.imagevalidation.common.rules;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({ @JsonSubTypes.Type(RuleSimpleCondition.class), @JsonSubTypes.Type(RuleCompositeCondition.class) })
public abstract class RuleCondition {}
