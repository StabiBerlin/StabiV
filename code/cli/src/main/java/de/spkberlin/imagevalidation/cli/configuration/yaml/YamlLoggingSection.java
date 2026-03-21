package de.spkberlin.imagevalidation.cli.configuration.yaml;

import java.util.List;

public class YamlLoggingSection {

    public static List<YamlLoggingVerbosityType> VERBOSITY_LEVELS = List.of(
        YamlLoggingVerbosityType.error,
        YamlLoggingVerbosityType.warning,
        YamlLoggingVerbosityType.info,
        YamlLoggingVerbosityType.debug,
        YamlLoggingVerbosityType.trace
    );

    private Boolean json;
    private YamlLoggingVerbosityType verbosity;

    public YamlLoggingSection() {
        json = false;
        verbosity = YamlLoggingVerbosityType.warning;
    }

    public Boolean getJson() {
        return this.json;
    }

    public void setJson(Boolean json) {
        this.json = json;
    }

    public YamlLoggingVerbosityType getVerbosity() {
        return this.verbosity;
    }

    public void setVerbosity(YamlLoggingVerbosityType verbosity) {
        this.verbosity = verbosity;
    }
}
