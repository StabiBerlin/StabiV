package de.spkberlin.imagevalidation.cli.configuration.yaml;

import de.spkberlin.imagevalidation.common.rules.Rule;
import java.util.List;

public class YamlValidationSection {

    private List<Rule> imageRules;

    public YamlValidationSection() {}

    public List<Rule> getImageRules() {
        return imageRules;
    }

    public void setImageRules(List<Rule> rules) {
        this.imageRules = rules;
    }
}
