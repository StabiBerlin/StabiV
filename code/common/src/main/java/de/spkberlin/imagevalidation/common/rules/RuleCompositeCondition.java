package de.spkberlin.imagevalidation.common.rules;

import java.util.List;

public class RuleCompositeCondition extends RuleCondition {

    private RuleCompositeType composite;
    private List<RuleCondition> conditions;

    public RuleCompositeCondition() {}

    public RuleCompositeType getComposite() {
        return this.composite;
    }

    public void setComposite(RuleCompositeType composite) {
        this.composite = composite;
    }

    public List<RuleCondition> getConditions() {
        return this.conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }
}
