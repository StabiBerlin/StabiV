package de.spkberlin.imagevalidation.common.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class PropertiesSerializer {

    public static String serializeProperties(Object properties) throws IOException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(properties);
    }
}
