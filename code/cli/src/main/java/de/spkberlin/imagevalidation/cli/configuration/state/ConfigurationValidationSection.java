package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationSection {

    private List<ImageRule> imageRules;

    public ConfigurationValidationSection() {
        imageRules = new ArrayList<>();
    }

    public List<ImageRule> getImageRules() {
        return imageRules;
    }

    public void setImageRules(List<ImageRule> rules) {
        this.imageRules = rules;
    }
}
