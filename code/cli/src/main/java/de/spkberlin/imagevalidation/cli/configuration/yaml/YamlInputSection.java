package de.spkberlin.imagevalidation.cli.configuration.yaml;

import java.util.List;

public class YamlInputSection {

    private List<String> includes;
    private List<String> excludes;

    public YamlInputSection() {}

    public List<String> getIncludes() {
        return this.includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return this.excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
}
