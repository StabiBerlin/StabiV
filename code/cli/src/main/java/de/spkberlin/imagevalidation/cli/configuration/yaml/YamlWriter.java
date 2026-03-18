package de.spkberlin.imagevalidation.cli.configuration.yaml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;

public class YamlWriter {

    public static String writeConfigurationFile(YamlConfiguration config) throws IOException {
        return YAMLMapper.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_ABSENT)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(config);
    }
}
