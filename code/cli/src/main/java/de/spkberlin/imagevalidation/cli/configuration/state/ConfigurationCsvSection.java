package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;

public class ConfigurationCsvSection {

    @Valid
    private ConfigurationPath path;

    private Boolean onlyFirstListEntry;

    public ConfigurationCsvSection() {
        this.onlyFirstListEntry = false;
    }

    public ConfigurationPath getPath() {
        return this.path;
    }

    public void setPath(ConfigurationPath path) {
        this.path = path;
    }

    public Boolean isOnlyFirstListEntry() {
        return this.onlyFirstListEntry;
    }

    public void setOnlyFirstListEntry(Boolean onlFirstListEntry) {
        this.onlyFirstListEntry = onlFirstListEntry;
    }
}
