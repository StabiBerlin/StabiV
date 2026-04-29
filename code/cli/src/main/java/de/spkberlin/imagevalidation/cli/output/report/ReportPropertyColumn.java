package de.spkberlin.imagevalidation.cli.output.report;

public class ReportPropertyColumn {

    private String label;
    private String property;

    public ReportPropertyColumn() {
        // empty
    }

    public ReportPropertyColumn(String label, String path) {
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
