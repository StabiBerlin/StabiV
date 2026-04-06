package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationSection {

    private List<ImageRule> imageRules;
    private Integer parallelThreads;
    private ConfigurationValidationDirectoryRules directoryRules;

    /**
     * Initialize configuration validation section.
     */
    public ConfigurationValidationSection() {
        imageRules = new ArrayList<>();
        parallelThreads = 4;
        directoryRules = new ConfigurationValidationDirectoryRules();
    }

    public List<ImageRule> getImageRules() {
        return imageRules;
    }

    public void setImageRules(List<ImageRule> rules) {
        this.imageRules = rules;
    }

    public Integer getParallelThreads() {
        return this.parallelThreads;
    }

    public void setParallelThreads(Integer parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    public ConfigurationValidationDirectoryRules getDirectoryRules() {
        return this.directoryRules;
    }

    public void setDirectoryRules(ConfigurationValidationDirectoryRules directoryRules) {
        this.directoryRules = directoryRules;
    }
}
