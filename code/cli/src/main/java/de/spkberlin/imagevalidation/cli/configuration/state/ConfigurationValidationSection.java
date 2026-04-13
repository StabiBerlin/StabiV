package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationSection {

    private List<ImageRule> imageRules;

    private ConfigurationValidationDirectoryRules directoryRules;

    /**
     * Initialize configuration validation section.
     */
    public ConfigurationValidationSection() {
        imageRules = new ArrayList<>();
        directoryRules = new ConfigurationValidationDirectoryRules();
    }

    public List<ImageRule> getImageRules() {
        return imageRules;
    }

    public void setImageRules(List<ImageRule> rules) {
        this.imageRules = rules;
    }

    public ConfigurationValidationDirectoryRules getDirectoryRules() {
        return this.directoryRules;
    }

    public void setDirectoryRules(ConfigurationValidationDirectoryRules directoryRules) {
        this.directoryRules = directoryRules;
    }
}
