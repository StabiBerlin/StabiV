package de.spkberlin.stabiv.common.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.spkberlin.stabiv.common.properties.PropertiesSerializer;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

public class PropertiesSerializerTest {

    /**
     * Check that an arbitrary property tree is converted to a pretty string by simplifying simple values
     * instead of printing everything as JSON.
     */
    @Test
    public void testToPrettyString() {
        assertEquals("test", PropertiesSerializer.toPrettyString("test"));
        assertEquals("test", PropertiesSerializer.toPrettyString(List.of("test")));
        assertEquals("", PropertiesSerializer.toPrettyString(Collections.emptyList()));
        assertEquals("[123]", PropertiesSerializer.toPrettyString(List.of(123)));
        assertEquals("[\"value1\",\"value2\"]", PropertiesSerializer.toPrettyString(List.of("value1", "value2")));

        Map<String, Object> properties = new TreeMap<>();
        properties.put("propertyA", Map.of("subproperty", "value"));
        properties.put("propertyB", List.of("value1", "value2"));

        assertEquals(
            """
            {"propertyA":{"subproperty":"value"},"propertyB":["value1","value2"]}
            """.trim(),
            PropertiesSerializer.toPrettyString(properties)
        );

        // non-property objects are translated by calling toString (but should not happen)
        assertTrue(PropertiesSerializer.toPrettyString(new Object()).startsWith("java.lang.Object@"));
    }

    /**
     * Check that an arbitrary property tree is correctly formatted as JSON with line breaks and indentation.
     * @throws IOException in case serialization fails
     */
    @Test
    public void testSerializeAsPrettyJson() throws IOException {
        Map<String, Object> properties = new TreeMap<>();
        properties.put("propertyA", Map.of("subproperty", "value"));
        properties.put("propertyB", List.of("value1", "value2"));

        assertEquals(
            """
            {
              "propertyA" : {
                "subproperty" : "value"
              },
              "propertyB" : [ "value1", "value2" ]
            }
            """.trim()
                .replace("\n", System.lineSeparator()),
            PropertiesSerializer.serializeAsPrettyJson(properties)
        );
    }
}
