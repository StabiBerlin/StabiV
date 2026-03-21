package de.spkberlin.imagevalidation.common.rules;

public class RuleCompositeNotCondition extends RuleCompositeCondition {

    private RuleCondition not;

    public RuleCompositeNotCondition() {
        // empty
    }

    public RuleCondition getNot() {
        return this.not;
    }

    public void setNot(RuleCondition not) {
        this.not = not;
    }
}
