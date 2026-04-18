package de.spkberlin.imagevalidation.common.validation.images.rules.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ImageRuleCompositeAndCondition extends ImageRuleCompositeCondition {

    @NotEmpty
    private List<@Valid ImageRuleCondition> and;

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
