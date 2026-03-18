package de.spkberlin.imagevalidation.jhove;

import edu.harvard.hul.ois.jhove.NisoImageMetadata;
import edu.harvard.hul.ois.jhove.Property;
import edu.harvard.hul.ois.jhove.PropertyArity;
import edu.harvard.hul.ois.jhove.RepInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse JHove validation object "RepInfo" to a JSON-like object tree containing only maps, lists and string values.
 */
public class RepInfoParser {

    private static final Logger logger = LoggerFactory.getLogger(RepInfoParser.class);

    private static final String NUMBER_PROPERTY_KEY = "Number";

    private static boolean isUniformPropertyArray(List<Property> properties) {
        // check whether there is a number property, indicating that this is a array
        Integer numberValue = properties
            .stream()
            .map(p -> (p.getName().equals(NUMBER_PROPERTY_KEY) ? ((int) p.getValue()) : -1))
            .reduce(Integer::max)
            .orElse(-1);

        List<Property> nonNumberProperties = properties
            .stream()
            .filter(p -> !p.getName().equals(NUMBER_PROPERTY_KEY))
            .collect(Collectors.toList());

        Set<String> nonNumberKeys = nonNumberProperties
            .stream()
            .map(p -> p.getName())
            .collect(Collectors.toSet());

        return nonNumberProperties.size() == numberValue && nonNumberKeys.size() == 1;
    }

    /**
     * Parse a property claiming to be a list.
     *
     * @param value the property value
     * @return the parsed list as List object or null
     */
    private static Object parsePropertyOfTypeList(Object value) {
        if (value instanceof List<?>) {
            List<?> valueList = (List<?>) value;
            boolean allItemsAreProperties = valueList.stream().allMatch(o -> o instanceof Property);
            if (allItemsAreProperties) {
                // list may be a key-value map or a list of properties
                @SuppressWarnings("unchecked")
                List<Property> propertyList = (List<Property>) valueList;
                if (isUniformPropertyArray(propertyList)) {
                    List<Property> nonNumberProperties = propertyList
                        .stream()
                        .filter(p -> !p.getName().equals(NUMBER_PROPERTY_KEY))
                        .collect(Collectors.toList());

                    return Map.of(
                        nonNumberProperties.get(0).getName(),
                        nonNumberProperties
                            .stream()
                            .map(p -> parseProperty(p))
                            .toList()
                    );
                }

                return propertyList
                    .stream()
                    .collect(
                        Collectors.toMap(
                            p -> p.getName(),
                            p -> parseProperty(p),
                            (p1, p2) -> {
                                logger.warn("JHove RepInfo contains property with duplicate key '{}'", p1);
                                return p1;
                            }
                        )
                    );
            }

            return valueList
                .stream()
                .map(item -> {
                    if (item instanceof Property) {
                        return parseProperty((Property) item);
                    }
                    return String.valueOf(item);
                })
                .toList();
        } else {
            logger.warn("JHove RepInfo contains property list of unknown type: " + value.toString());
        }
        return null;
    }

    /**
     * Parse property claiming to be an array.
     *
     * @param value the property value
     * @return the parsed array as List object or null
     */
    private static Object parsePropertyOfTypeArray(Object value) {
        if (value instanceof Property[]) {
            Property[] array = (Property[]) value;
            return parsePropertyOfTypeList(Arrays.asList(array));
        } else if (value instanceof String[]) {
            return Arrays.asList((String[]) value);
        } else if (value instanceof int[]) {
            return Arrays.stream((int[]) value).boxed().toList();
        } else if (value instanceof long[]) {
            return Arrays.stream((long[]) value).boxed().toList();
        } else {
            logger.warn("JHove RepInfo contains property array of unknown type: " + value.toString());
        }
        return null;
    }

    /**
     * Parse a JHove property object into a simple map, list or string value.
     *
     * @param property the JHove property object
     * @return the simple map, list or string value
     */
    private static Object parseProperty(Property property) {
        PropertyArity arity = property.getArity();
        String propertyKey = property.getName();
        Object propertyValue = property.getValue();
        logger.trace("parse JHove property '{}' of arity '{}'", propertyKey, arity.toString());
        if (arity == PropertyArity.SCALAR) {
            if (propertyValue instanceof NisoImageMetadata) {
                NisoImageMetadata metadata = (NisoImageMetadata) property.getValue();
                return NisoImageMetadataHelper.nisoImageMetadataToMap(metadata);
            } else {
                return String.valueOf(propertyValue);
            }
        } else if (arity == PropertyArity.ARRAY) {
            return parsePropertyOfTypeArray(propertyValue);
        } else if (arity == PropertyArity.LIST) {
            return parsePropertyOfTypeList(propertyValue);
        }
        return null;
    }

    /**
     * Parses all properties provided by the JHove RepInfo object.
     *
     * @param jhoveProperties the map of properties as provided by Jhove
     * @return an object tree consisting of lists, maps or raw values converted to string
     */
    private static Map<String, Object> parseProperties(Map<String, Property> jhoveProperties) {
        Map<String, Object> tree = new HashMap<>();

        for (Map.Entry<String, Property> entry : jhoveProperties.entrySet()) {
            tree.put(entry.getKey(), parseProperty(entry.getValue()));
        }

        return tree;
    }

    /**
     * Converts an integer value representing a ternary value (true, false, undetermined) to a string of true, false, undetermined.
     *
     * @param value
     *            the int value
     * @return the string value
     */
    private static String ternaryValueToString(int value) {
        if (value == RepInfo.TRUE) {
            return "true";
        } else if (value == RepInfo.FALSE) {
            return "false";
        } else if (value == RepInfo.UNDETERMINED) {
            return "undetermined";
        }
        return "invalid";
    }

    /**
     * Extracts the tree of properties from a JHove RepInfo object by parsing the hierarchical property structure
     * and adding non-hierarchical base properties provided directly by Jhove (e.g. valid, wellformed).
     *
     * @param info
     *            the RepInfo object
     * @return the flat map of properties and values
     */
    public static Map<String, Object> repInfoToPropertyTree(RepInfo info) {
        boolean successfulParsing = Objects.nonNull(info.getModule());

        Map<String, Object> propertyMap = parseProperties(info.getProperty());
        propertyMap.put("mimetype", info.getMimeType());
        propertyMap.put("format", info.getFormat());
        propertyMap.put("version", info.getVersion());
        propertyMap.put("size", info.getSize());
        propertyMap.put("profile", info.getProfile());
        propertyMap.put("module", Objects.nonNull(info.getModule()) ? info.getModule().getName() : "none");
        propertyMap.put("wellformed", successfulParsing ? ternaryValueToString(info.getWellFormed()) : "false");
        propertyMap.put("valid", successfulParsing ? ternaryValueToString(info.getValid()) : "false");

        return propertyMap;
    }
}
