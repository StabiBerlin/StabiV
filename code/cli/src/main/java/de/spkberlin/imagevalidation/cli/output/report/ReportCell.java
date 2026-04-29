package de.spkberlin.imagevalidation.cli.output.report;

public class ReportCell {

    private String text;
    private Boolean highlighted;

    public ReportCell() {
        this.highlighted = false;
    }

    public ReportCell(String text, Boolean highlighted) {
        this.text = text;
        this.highlighted = highlighted;
    }

    public static ReportCell empty() {
        return new ReportCell();
    }

    public static ReportCell of(String text) {
        return new ReportCell(text, false);
    }

    public static ReportCell of(String text, Boolean highlighted) {
        return new ReportCell(text, highlighted);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(Boolean highlighted) {
        this.highlighted = highlighted;
    }
}
