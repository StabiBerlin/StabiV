package de.spkberlin.imagevalidation.cli.output.report;

import java.util.List;

public enum ReportCellHighlightType {
    none,
    error,
    warning;

    /**
     * Determine the highlight type from a potentially empty list of errors and warnings.
     *
     * @param errors the list of errors (may be empty)
     * @param warnings the list warnings (may be empty)
     * @return error highlight type if there are errors, warning highlight type if there are warnings, else none type
     */
    public static ReportCellHighlightType fromErrorsAndWarnings(List<String> errors, List<String> warnings) {
        return !errors.isEmpty()
            ? ReportCellHighlightType.error
            : !warnings.isEmpty()
                ? ReportCellHighlightType.warning
                : ReportCellHighlightType.none;
    }
}
