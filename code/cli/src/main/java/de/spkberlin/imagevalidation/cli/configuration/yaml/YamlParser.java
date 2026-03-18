package de.spkberlin.imagevalidation.cli.configuration.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Path;

public class YamlParser {

    public static YamlConfiguration parseConfigurationFile(Path filepath) throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(filepath.toFile(), YamlConfiguration.class);
    }
}
