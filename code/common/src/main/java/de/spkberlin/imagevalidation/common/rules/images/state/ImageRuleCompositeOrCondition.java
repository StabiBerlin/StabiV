package de.spkberlin.imagevalidation.common.rules.images.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ImageRuleCompositeOrCondition extends ImageRuleCompositeCondition {

    @NotEmpty
    private List<@Valid ImageRuleCondition> or;

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
