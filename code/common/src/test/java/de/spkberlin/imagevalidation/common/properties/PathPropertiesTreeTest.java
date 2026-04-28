package de.spkberlin.imagevalidation.common.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PathPropertiesTreeTest {

    /**
     * Check that simple map and array properties are translated into correct object tree.
     *
     * @throws IOException in case object mapping fails
     */
    @Test
    public void testSimpleTree() throws IOException {
        Set<PathProperty> properties = Set.of(
            new PathProperty("test/array[1]/value", "text"),
            new PathProperty("test/array[3]/value", "other")
        );

        Map<String, Object> tree = PathPropertiesTree.buildTreeFromPaths(properties);
        assertEquals(
            """
                {"test":{"array":[null,{"value":"text"},null,{"value":"other"}]}}
            """.trim(),
            new ObjectMapper().writeValueAsString(tree)
        );
    }

    /**
     * Check that simple array properties are translated into correct object tree.
     *
     * @throws IOException in case object mapping fails
     */
    @Test
    public void testSimpleArray() throws IOException {
        Set<PathProperty> properties = Set.of(new PathProperty("test[0]", "value1"), new PathProperty("test[1]", "value2"));
        Map<String, Object> tree = PathPropertiesTree.buildTreeFromPaths(properties);

        assertEquals(
            """
                {"test":["value1","value2"]}
            """.trim(),
            new ObjectMapper().writeValueAsString(tree)
        );
    }

    /**
     * Check that incosistent property (which may be either map or array) causes an exception.
     */
    @Test
    public void testIncosistentProperties() {
        LinkedHashSet<PathProperty> properties = new LinkedHashSet<>();
        properties.add(new PathProperty("test/array[1]/value", "text"));
        properties.add(new PathProperty("test/array/something", "other"));

        assertThrows(IllegalArgumentException.class, () -> PathPropertiesTree.buildTreeFromPaths(properties));
        properties.reversed();
        assertThrows(IllegalArgumentException.class, () -> PathPropertiesTree.buildTreeFromPaths(properties));
    }
}
