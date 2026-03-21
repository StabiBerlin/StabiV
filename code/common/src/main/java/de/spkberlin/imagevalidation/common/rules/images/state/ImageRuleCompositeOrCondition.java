package de.spkberlin.imagevalidation.common.rules.images.state;

import java.util.List;

public class ImageRuleCompositeOrCondition extends ImageRuleCompositeCondition {

    private List<ImageRuleCondition> or;

    public ImageRuleCompositeOrCondition() {
        // empty
    }

    public List<ImageRuleCondition> getOr() {
        return this.or;
    }

    public void setOr(List<ImageRuleCondition> or) {
        this.or = or;
    }
}
