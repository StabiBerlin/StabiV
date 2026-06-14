package de.spkberlin.stabiv.cli.configuration.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationInputSection {

    private Path baseDirectory;

    @NotEmpty
    private List<@Valid ConfigurationPath> includes;

    private List<@Valid ConfigurationPath> excludes;

    @Valid
    private ConfigurationPath path;

    public ConfigurationInputSection() {
        includes = new ArrayList<>();
        excludes = new ArrayList<>();
    }

    public Path getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(Path basepath) {
        this.baseDirectory = basepath;
    }

    public List<ConfigurationPath> getIncludes() {
        return this.includes;
    }

    public void setIncludes(List<ConfigurationPath> includes) {
        this.includes = includes;
    }

    public List<ConfigurationPath> getExcludes() {
        return this.excludes;
    }

    public void setExcludes(List<ConfigurationPath> excludes) {
        this.excludes = excludes;
    }

    public ConfigurationPath getPath() {
        return this.path;
    }

    public void setPath(ConfigurationPath path) {
        this.path = path;
    }
}
