package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.rules.files.state.FileRule;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationSection {

    private List<FileRule> imageRules;

    public ConfigurationValidationSection() {
        imageRules = new ArrayList<>();
    }

    public List<FileRule> getImageRules() {
        return imageRules;
    }

    public void setImageRules(List<FileRule> rules) {
        this.imageRules = rules;
    }
}
