package de.spkberlin.imagevalidation.common.rules.files.state;

public class FileRuleCompositeNotCondition extends FileRuleCompositeCondition {

    private FileRuleCondition not;

    public FileRuleCompositeNotCondition() {
        // empty
    }

    public FileRuleCondition getNot() {
        return this.not;
    }

    public void setNot(FileRuleCondition not) {
        this.not = not;
    }
}
