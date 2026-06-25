package de.spkberlin.stabiv.cli.configuration.state;

import de.spkberlin.stabiv.exiftool.ExiftoolExtractor;
import java.nio.file.Path;
import java.util.List;

public class ConfigurationExtractionExiftoolSection {

    private Path path;

    private List<String> arguments;

    public ConfigurationExtractionExiftoolSection() {
        this.path = ExiftoolExtractor.DEFAULT_CMD;
        this.arguments = ExiftoolExtractor.DEFAULT_CLI_ARGS;
    }

    public Path getPath() {
        return this.path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
}
