package de.spkberlin.stabiv.cli.configuration.state;

import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsecutiveNumberingCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsistencyCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationValidationDirectoryRules {

    private List<@Valid DirectoryConsistencyCondition> consistentProperties;

    @Valid
    private DirectoryConsecutiveNumberingCondition consecutiveNumbering;

    private List<@Valid DirectoryPropertyDeviationsRule> propertyDeviations;

    public ConfigurationValidationDirectoryRules() {
        this.consistentProperties = new ArrayList<>();
    }

    public List<DirectoryConsistencyCondition> getConsistentProperties() {
        return this.consistentProperties;
    }

    public void setConsistentProperties(List<DirectoryConsistencyCondition> consistentProperties) {
        this.consistentProperties = consistentProperties;
    }

    public DirectoryConsecutiveNumberingCondition getConsecutiveNumbering() {
        return consecutiveNumbering;
    }

    public void setConsecutiveNumbering(DirectoryConsecutiveNumberingCondition consecutiveNumbering) {
        this.consecutiveNumbering = consecutiveNumbering;
    }

    public List<DirectoryPropertyDeviationsRule> getPropertyDeviations() {
        return this.propertyDeviations;
    }

    public void setPropertyDeviations(List<DirectoryPropertyDeviationsRule> propertyDeviations) {
        this.propertyDeviations = propertyDeviations;
    }
}
