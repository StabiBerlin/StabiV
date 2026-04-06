package de.spkberlin.imagevalidation.common.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

public class PropertiesSerializer {

    /**
     * Serialize a property object with ObjectMapper as pretty string (with indendation).
     *
     * @param properties the property object
     * @return the serialized property object as pretty string
     * @throws IOException in case serialization fails
     */
    public static String serializeAsPrettyJson(Object properties) throws IOException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(properties);
    }

    /**
     * Serialize a hierarchical property object (JSON object) to a simple string.
     *
     * <p>In contrast to ObjectMapper, the raw string value is returned if the object itself is a string, or if the object is a list with
     * exactly one string entry.
     *
     * @param property the property object
     * @return the serialized property object as string
     */
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
