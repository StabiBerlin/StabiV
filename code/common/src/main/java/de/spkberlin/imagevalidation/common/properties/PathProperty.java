package de.spkberlin.imagevalidation.common.properties;

/**
 * Stores a property as a combination of path and value.
 *
 * <p>The path needs to be specified as a simplified JSON path, e.g. "property/subproperty/array[0]/something". Hierarchy levels are
 * encoded as slash and array indices are referenced via brackets.
 *
 * @param path the path to the property as a simplified JSON path (e.g. "property/arrayproperty[0]/subproperty")
 * @param value the extracted property value as string
 */
public record PathProperty(String path, String value) {}
