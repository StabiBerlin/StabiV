package de.spkberlin.imagevalidation.cli.output.report;

import java.util.List;

public enum ReportCellHighlightType {
    none,
    error,
    warning;

    public static ReportCellHighlightType fromErrorsAndWarnings(List<String> errors, List<String> warnings) {
        return !errors.isEmpty()
            ? ReportCellHighlightType.error
            : !warnings.isEmpty()
                ? ReportCellHighlightType.warning
                : ReportCellHighlightType.none;
    }
}
