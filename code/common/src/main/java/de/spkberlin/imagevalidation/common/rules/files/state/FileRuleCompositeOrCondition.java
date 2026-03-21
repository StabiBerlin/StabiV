package de.spkberlin.imagevalidation.common.rules.files.state;

import java.util.List;

public class FileRuleCompositeOrCondition extends FileRuleCompositeCondition {

    private List<FileRuleCondition> or;

    public FileRuleCompositeOrCondition() {
        // empty
    }

    public List<FileRuleCondition> getOr() {
        return this.or;
    }

    public void setOr(List<FileRuleCondition> or) {
        this.or = or;
    }
}
