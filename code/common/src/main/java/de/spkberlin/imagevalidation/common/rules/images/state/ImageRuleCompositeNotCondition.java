package de.spkberlin.imagevalidation.common.rules.images.state;

public class ImageRuleCompositeNotCondition extends ImageRuleCompositeCondition {

    private ImageRuleCondition not;

    public ImageRuleCompositeNotCondition() {
        // empty
    }

    public ImageRuleCondition getNot() {
        return this.not;
    }

    public void setNot(ImageRuleCondition not) {
        this.not = not;
    }
}
