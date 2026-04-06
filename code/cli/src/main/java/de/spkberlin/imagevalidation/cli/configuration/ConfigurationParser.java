package de.spkberlin.imagevalidation.cli.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationRoot;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationParser {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);

    /**
     * Parse a yaml configuration file via ObjectMapper.
     *
     * @param filepath the filepath to the yaml configuration file
     * @return the parsed configuration object
     * @throws IOException in case parsing fails
     */
    public static ConfigurationRoot parseConfigurationFile(Path filepath) throws IOException {
        logger.debug("parse yaml configuration file from '{}'", filepath);
        ConfigurationRoot config = new ObjectMapper(new YAMLFactory()).readValue(filepath.toFile(), ConfigurationRoot.class);
        logger.trace("read yaml configuration:\n\n{}", ConfigurationWriter.writeConfigurationFile(config));
        return config;
    }
}
