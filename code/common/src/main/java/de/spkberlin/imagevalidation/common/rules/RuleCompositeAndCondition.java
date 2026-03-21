package de.spkberlin.imagevalidation.common.rules;

import java.util.List;

public class RuleCompositeAndCondition extends RuleCompositeCondition {

    private List<RuleCondition> and;

    public RuleCompositeAndCondition() {
        // empty
    }

    public List<RuleCondition> getAnd() {
        return this.and;
    }

    public void setAnd(List<RuleCondition> and) {
        this.and = and;
    }
}
