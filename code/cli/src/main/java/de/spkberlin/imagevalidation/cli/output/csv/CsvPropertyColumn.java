package de.spkberlin.imagevalidation.cli.output.csv;

public class CsvPropertyColumn {

    private String label;

    private String property;

    public CsvPropertyColumn(String label, String path) {
        this.label = label;
        this.property = path;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String path) {
        this.property = path;
    }
}
