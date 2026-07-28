package de.spkberlin.stabiv.cli.path;

import java.nio.file.Path;

public class PathPattern {

    private Path baseDirectory;
    private String pattern;

    public PathPattern(String pattern, Path baseDirectory) {
        this.pattern = pattern;
        this.baseDirectory = baseDirectory;
    }

    public Path getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
