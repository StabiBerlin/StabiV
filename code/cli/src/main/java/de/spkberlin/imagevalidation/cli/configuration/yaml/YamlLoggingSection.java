package de.spkberlin.imagevalidation.cli.configuration.yaml;

public class YamlLoggingSection {

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
