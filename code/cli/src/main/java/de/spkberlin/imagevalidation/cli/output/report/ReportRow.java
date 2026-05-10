package de.spkberlin.imagevalidation.cli.output.report;

import java.util.Collections;
import java.util.List;

public class ReportRow {

    private List<ReportCell> cells;

    private ReportRowType type;

    private Boolean valid;

    public ReportRow(List<ReportCell> cells, ReportRowType type, Boolean valid) {
        this.cells = cells;
        this.type = type;
        this.valid = valid;
    }

    public static ReportRow empty() {
        return new ReportRow(Collections.emptyList(), ReportRowType.empty, null);
    }

    public static ReportRow ofHeader(List<ReportCell> cells) {
        return new ReportRow(cells, ReportRowType.header, null);
    }

    public static ReportRow ofSummary(List<ReportCell> cells) {
        return new ReportRow(cells, ReportRowType.summary, null);
    }

    public static ReportRow ofDirectory(List<ReportCell> cells, Boolean valid) {
        return new ReportRow(cells, ReportRowType.directory, valid);
    }

    public static ReportRow ofImage(List<ReportCell> cells, Boolean valid) {
        return new ReportRow(cells, ReportRowType.image, valid);
    }

    public List<ReportCell> getCells() {
        return this.cells;
    }

    public ReportRowType getType() {
        return this.type;
    }

    public Boolean isValid() {
        return valid;
    }
}
