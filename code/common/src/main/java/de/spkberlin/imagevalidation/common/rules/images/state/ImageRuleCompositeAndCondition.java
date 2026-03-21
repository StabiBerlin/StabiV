package de.spkberlin.imagevalidation.common.rules.images.state;

import java.util.List;

public class ImageRuleCompositeAndCondition extends ImageRuleCompositeCondition {

    private List<ImageRuleCondition> and;

    public ImageRuleCompositeAndCondition() {
        // empty
    }

    public List<ImageRuleCondition> getAnd() {
        return this.and;
    }

    public void setAnd(List<ImageRuleCondition> and) {
        this.and = and;
    }
}
