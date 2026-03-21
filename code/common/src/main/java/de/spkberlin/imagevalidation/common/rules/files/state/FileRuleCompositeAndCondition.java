package de.spkberlin.imagevalidation.common.rules.files.state;

import java.util.List;

public class FileRuleCompositeAndCondition extends FileRuleCompositeCondition {

    private List<FileRuleCondition> and;

    public FileRuleCompositeAndCondition() {
        // empty
    }

    public List<FileRuleCondition> getAnd() {
        return this.and;
    }

    public void setAnd(List<FileRuleCondition> and) {
        this.and = and;
    }
}
