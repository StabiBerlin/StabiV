package de.spkberlin.imagevalidation.common.validation.directories.state;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DirectoryPropertyDeviationsResult {

    Map<Path, List<DirectoryPropertyDeviationsImageResult>> imageResults;

    public DirectoryPropertyDeviationsResult(Map<Path, List<DirectoryPropertyDeviationsImageResult>> imageResults) {
        this.imageResults = imageResults;
    }

    public Map<Path, List<DirectoryPropertyDeviationsImageResult>> getImageResults() {
        return this.imageResults;
    }

    public void setImageResults(Map<Path, List<DirectoryPropertyDeviationsImageResult>> imageResults) {
        this.imageResults = imageResults;
    }
}
