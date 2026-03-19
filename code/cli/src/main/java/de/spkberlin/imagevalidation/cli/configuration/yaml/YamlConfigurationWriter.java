package de.spkberlin.imagevalidation.cli.configuration.yaml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YamlConfigurationWriter {

    private static final Logger logger = LoggerFactory.getLogger(YamlConfigurationWriter.class);

    public static String writeConfigurationFile(YamlConfiguration config) throws IOException {
        logger.debug("serialize yaml configuration");
        return YAMLMapper.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_ABSENT)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(config);
    }
}
