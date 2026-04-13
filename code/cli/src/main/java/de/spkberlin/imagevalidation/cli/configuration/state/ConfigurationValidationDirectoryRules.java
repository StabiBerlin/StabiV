package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationDirectoryRules {

    private List<String> consistentProperties;

    @Valid
    private ConfigurationValidationConsecutiveNumbering consecutiveNumbering;

    public ConfigurationValidationDirectoryRules() {
        this.consistentProperties = new ArrayList<>();
    }

    public List<String> getConsistentProperties() {
        return this.consistentProperties;
    }

    public void setConsistentProperties(List<String> consistentProperties) {
        this.consistentProperties = consistentProperties;
    }

    public ConfigurationValidationConsecutiveNumbering getConsecutiveNumbering() {
        return consecutiveNumbering;
    }

    public void setConsecutiveNumbering(ConfigurationValidationConsecutiveNumbering consecutiveNumbering) {
        this.consecutiveNumbering = consecutiveNumbering;
    }
}
