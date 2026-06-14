package de.spkberlin.stabiv.cli.configuration.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import java.nio.file.Path;

public class ConfigurationPath {

    private Path baseDirectory;

    @NotBlank
    private String path;

    public ConfigurationPath() {
        // empty
    }

    public ConfigurationPath(String path, Path baseDirectory) {
        this.path = path;
        this.baseDirectory = baseDirectory;
    }

    @JsonCreator
    public ConfigurationPath(String path) {
        this.path = path;
    }

    public Path getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
