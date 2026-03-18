package de.spkberlin.imagevalidation.cli.configuration.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YamlParser {

    private static final Logger logger = LoggerFactory.getLogger(YamlParser.class);

    public static YamlConfiguration parseConfigurationFile(Path filepath) throws IOException {
        logger.debug("parse yaml configuration file from '{}'", filepath);
        YamlConfiguration config = new ObjectMapper(new YAMLFactory()).readValue(filepath.toFile(), YamlConfiguration.class);
        logger.trace("read yaml configuration:\n\n{}", YamlWriter.writeConfigurationFile(config));
        return config;
    }
}
