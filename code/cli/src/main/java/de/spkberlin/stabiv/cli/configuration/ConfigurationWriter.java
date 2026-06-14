package de.spkberlin.stabiv.cli.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationRoot;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationWriter {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationWriter.class);

    /**
     * Convert a configuration to yaml format via ObjectMapper.
     *
     * @param config the configuration
     * @return a string of the configuration in yaml format
     * @throws IOException in case conversion fails
     */
    public static String writeFile(ConfigurationRoot config) throws IOException {
        logger.debug("serialize yaml configuration");
        return YAMLMapper.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_ABSENT)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(config);
    }
}
