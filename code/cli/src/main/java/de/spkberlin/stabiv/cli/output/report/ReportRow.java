package de.spkberlin.stabiv.cli.output.report;

import java.util.Collections;
import java.util.List;

public class ReportRow {

    private List<ReportCell> cells;

    private ReportRowType type;

    private Boolean valid;

    /**
     * Create a report row from a list of cells, a type and whether the row corresponds to a valid or invalid directory or image.
     *
     * @param cells the row cells
     * @param type the row type
     * @param valid whether the row corresponds to a valid or invalid directory or image
     */
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

    public static ReportRow ofMetadata(List<ReportCell> cells) {
        return new ReportRow(cells, ReportRowType.metadata, null);
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
