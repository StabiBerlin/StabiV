package de.spkberlin.imagevalidation.common.properties;

/**
 * Stores a property as a combination of path and value.
 *
 * <p>The path needs to be specified as a simplified JSON path, e.g. "property/subproperty/array[0]/something". Hierarchy levels are
 * encoded as slash and array indices are referenced via brackets.
 */
public class PathProperty {

    private String path;
    private String value;

    /**
     * Create new path property instance.
     *
     * @param path the path to the property as a simplified JSON path (e.g. "property/arrayproperty[0]/subproperty")
     * @param value the extracted property value as string
     */
    public PathProperty(String path, String value) {
        this.path = path;
        this.value = value;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
