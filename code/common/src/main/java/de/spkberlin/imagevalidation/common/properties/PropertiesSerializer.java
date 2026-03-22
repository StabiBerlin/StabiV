package de.spkberlin.imagevalidation.common.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

public class PropertiesSerializer {

    public static String serializeAsPrettyJson(Object properties) throws IOException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(properties);
    }

    public static String toPrettyString(Object property) {
        try {
            if (property instanceof String) {
                return (String) property;
            }
            if (property instanceof List) {
                List<?> propertyList = (List<?>) property;
                if (propertyList.size() == 0) {
                    return "";
                }

                if (propertyList.size() == 1 && propertyList.get(0) instanceof String) {
                    return (String) propertyList.get(0);
                }

                return new ObjectMapper().writeValueAsString(property);
            }
            return new ObjectMapper().writeValueAsString(property);
        } catch (JsonProcessingException e) {
            return property.toString();
        }
    }
}
