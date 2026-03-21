package de.spkberlin.imagevalidation.cli.configuration.state;

import java.util.List;

public class ConfigurationLoggingSection {

    public static List<ConfigurationLoggingVerbosityType> VERBOSITY_LEVELS = List.of(
        ConfigurationLoggingVerbosityType.error,
        ConfigurationLoggingVerbosityType.warning,
        ConfigurationLoggingVerbosityType.info,
        ConfigurationLoggingVerbosityType.debug,
        ConfigurationLoggingVerbosityType.trace
    );

    private Boolean json;
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
