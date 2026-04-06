package de.spkberlin.imagevalidation.cli.configuration.state;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationDirectoryRules {

    private List<String> consistentProperties;

    public ConfigurationValidationDirectoryRules() {
        this.consistentProperties = new ArrayList<>();
    }

    public List<String> getConsistentProperties() {
        return this.consistentProperties;
    }

    public void setConsistentProperties(List<String> consistentProperties) {
        this.consistentProperties = consistentProperties;
    }
}
