package de.spkberlin.imagevalidation.common.validation.images.rules.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ImageRuleCompositeNotCondition extends ImageRuleCompositeCondition {

    @Valid
    @NotNull
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
