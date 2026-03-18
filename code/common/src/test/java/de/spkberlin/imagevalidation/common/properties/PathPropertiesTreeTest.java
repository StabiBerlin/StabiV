package de.spkberlin.imagevalidation.common.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PathPropertiesTreeTest {

    @Test
    public void testSimpleTree() throws IOException {
        Set<PathProperty> properties = Set.of(
            new PathProperty("test/array[1]/value", "text"),
            new PathProperty("test/array[3]/value", "other")
        );

        Map<String, Object> tree = PathPropertiesTree.buildTreeFromPaths(properties);

        String json = new ObjectMapper().writeValueAsString(tree);

        assertEquals(
            """
                {"test":{"array":[null,{"value":"text"},null,{"value":"other"}]}}
            """.trim(),
            json
        );
    }

    @Test
    public void testIncosistentProperties() {
        Set<PathProperty> properties = Set.of(
            new PathProperty("test/array[1]/value", "text"),
            new PathProperty("test/array/something", "other")
        );

        assertThrows(RuntimeException.class, () -> PathPropertiesTree.buildTreeFromPaths(properties));
    }
}
