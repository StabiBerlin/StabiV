package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ConfigurationCsvSection {

    @Valid
    private ConfigurationPath path;

    @NotNull
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
