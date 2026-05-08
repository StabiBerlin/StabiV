package de.spkberlin.imagevalidation.cli.output.report;

import java.util.List;
import java.util.stream.Stream;

public class ReportCell {

    private ReportCellValueType valueType;
    private ReportCellHighlightType highlightType;
    private Integer integer;
    private List<String> list;
    private String text;

    /**
     * Create a new report cell based on all properties.
     *
     * @param valueType the value type
     * @param highlightType the highlighting type
     * @param integer the integer value, if any
     * @param text the text value, if any
     * @param list the list value, if any
     */
    public ReportCell(
        ReportCellValueType valueType,
        ReportCellHighlightType highlightType,
        Integer integer,
        String text,
        List<String> list
    ) {
        this.valueType = valueType;
        this.highlightType = highlightType;
        this.text = text;
        this.integer = integer;
        this.list = list;
    }

    public static ReportCell empty() {
        return new ReportCell(ReportCellValueType.empty, ReportCellHighlightType.none, null, null, null);
    }

    public static ReportCell of(String text) {
        return new ReportCell(ReportCellValueType.text, ReportCellHighlightType.none, null, text, null);
    }

    /**
     * Creates a report cell for a single boolean describing whether the validation was successful or not.
     *
     * @param passed whether the validation of directory or file passed
     * @return the report cell containing "true" or "false"
     */
    public static ReportCell of(Boolean passed) {
        return new ReportCell(
            ReportCellValueType.text,
            !passed ? ReportCellHighlightType.error : ReportCellHighlightType.none,
            null,
            String.valueOf(passed),
            null
        );
    }

    /**
     * Creates a report cell combining a list of errors and warnings.
     *
     * @param errors the list of errors
     * @param warnings the list of warnings
     * @return the cell listing all errors and warnings
     */
    public static ReportCell of(List<String> errors, List<String> warnings) {
        return new ReportCell(
            ReportCellValueType.list,
            !errors.isEmpty()
                ? ReportCellHighlightType.error
                : !warnings.isEmpty()
                    ? ReportCellHighlightType.warning
                    : ReportCellHighlightType.none,
            null,
            null,
            Stream.concat(errors.stream(), warnings.stream()).toList()
        );
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public ReportCellValueType getValueType() {
        return this.valueType;
    }

    public void setValueType(ReportCellValueType valueType) {
        this.valueType = valueType;
    }

    public ReportCellHighlightType getHighlightType() {
        return this.highlightType;
    }

    public void setHighlightType(ReportCellHighlightType highlightType) {
        this.highlightType = highlightType;
    }
}
