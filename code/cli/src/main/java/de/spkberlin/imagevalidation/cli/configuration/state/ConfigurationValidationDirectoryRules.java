package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryPropertyDeviationsEntry;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationDirectoryRules {

    private List<String> consistentProperties;

    @Valid
    private ConfigurationValidationConsecutiveNumbering consecutiveNumbering;

    private List<@Valid DirectoryPropertyDeviationsEntry> propertyDeviations;

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

    public List<DirectoryPropertyDeviationsEntry> getPropertyDeviations() {
        return this.propertyDeviations;
    }

    public void setPropertyDeviations(List<DirectoryPropertyDeviationsEntry> propertyDeviations) {
        this.propertyDeviations = propertyDeviations;
    }
}
