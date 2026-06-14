package de.spkberlin.stabiv.cli.configuration.state;

import jakarta.validation.Valid;
import java.util.List;

public class ConfigurationLoggingSection {

    public static List<ConfigurationLoggingVerbosityType> VERBOSITY_LEVELS = List.of(
        ConfigurationLoggingVerbosityType.error,
        ConfigurationLoggingVerbosityType.warning,
        ConfigurationLoggingVerbosityType.info,
        ConfigurationLoggingVerbosityType.debug,
        ConfigurationLoggingVerbosityType.trace
    );

    @Valid
    private Boolean json;

    @Valid
    private ConfigurationLoggingVerbosityType verbosity;

    public ConfigurationLoggingSection() {
        json = false;
        verbosity = ConfigurationLoggingVerbosityType.warning;
    }

    public Boolean getJson() {
        return this.json;
    }

    public void setJson(Boolean json) {
        this.json = json;
    }

    public ConfigurationLoggingVerbosityType getVerbosity() {
        return this.verbosity;
    }

    public void setVerbosity(ConfigurationLoggingVerbosityType verbosity) {
        this.verbosity = verbosity;
    }
}
