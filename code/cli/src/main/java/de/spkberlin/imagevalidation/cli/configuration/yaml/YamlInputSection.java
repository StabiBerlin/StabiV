package de.spkberlin.imagevalidation.cli.configuration.yaml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class YamlInputSection {

    private Path baseDirectory;
    private List<String> includes;
    private List<String> excludes;

    public YamlInputSection() {
        includes = new ArrayList<>();
        excludes = new ArrayList<>();
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

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
