package de.spkberlin.imagevalidation.common.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathPropertiesTree {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("(.+)\\[(\\d+)]");

    public static Map<String, Object> buildTreeFromPaths(Set<PathProperty> properties) throws RuntimeException {
        Map<String, Object> tree = new HashMap<>();

        for (PathProperty property : properties) {
            insertProperty(tree, Arrays.asList(property.path().split("/")), property.value());
        }

        return tree;
    }

    private static void insertProperty(Map<String, Object> tree, List<String> path, String value) {
        if (ARRAY_PATTERN.matcher(path.getFirst()).matches()) {
            insertArrayProperty(tree, path, value);
        } else {
            insertMapProperty(tree, path, value);
        }
    }

    private static void insertMapProperty(Map<String, Object> tree, List<String> path, String value) {
        String pathPart = path.getFirst();
        if (path.size() == 1) {
            // this is the last path element
            tree.put(pathPart, value);
        } else {
            // there are more subproperties
            if (!tree.containsKey(pathPart)) {
                tree.put(pathPart, new HashMap<>());
            } else if (!(tree.get(pathPart) instanceof Map)) {
                new RuntimeException("cannot insert subproperty for property that was previously defined as something other than an map");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> subtreeMap = (Map<String, Object>) tree.get(pathPart);

            insertProperty(subtreeMap, path.subList(1, path.size()), value);
        }
    }

    private static void insertArrayProperty(Map<String, Object> tree, List<String> path, String value) {
        Matcher arrayMatcher = ARRAY_PATTERN.matcher(path.getFirst());
        arrayMatcher.matches();
        String pathPart = arrayMatcher.group(1);
        Integer arrayIndex = Integer.parseInt(arrayMatcher.group(2));

        if (!tree.containsKey(pathPart)) {
            tree.put(pathPart, new ArrayList<>());
        } else if (!(tree.get(pathPart) instanceof List)) {
            new RuntimeException("cannot insert array element into property that was previously defined as something other than an array");
        }

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) tree.get(pathPart);

        // extend list to sufficient capacity
        while (list.size() <= arrayIndex) {
            list.add(null);
        }

        if (path.size() == 1) {
            // this is the last path element
            list.set(arrayIndex, value);
        } else {
            // there are more subproperties

            // create a new map if not yet created
            if (Objects.isNull(list.get(arrayIndex))) {
                list.set(arrayIndex, new HashMap<>());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> arrayElementMap = (Map<String, Object>) list.get(arrayIndex);

            insertProperty(arrayElementMap, path.subList(1, path.size()), value);
        }
    }
}
