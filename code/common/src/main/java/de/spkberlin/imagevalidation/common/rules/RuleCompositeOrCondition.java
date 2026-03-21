package de.spkberlin.imagevalidation.common.rules;

import java.util.List;

public class RuleCompositeOrCondition extends RuleCompositeCondition {

    private List<RuleCondition> or;

    public RuleCompositeOrCondition() {
        // empty
    }

    public List<RuleCondition> getOr() {
        return this.or;
    }

    public void setOr(List<RuleCondition> or) {
        this.or = or;
    }
}
