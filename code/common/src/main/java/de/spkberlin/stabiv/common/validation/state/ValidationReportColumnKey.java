package de.spkberlin.stabiv.common.validation.state;

/**
 * This record class is used to store report columns with semantic equality (equals returns true if both label and property matches).
 *
 * <p>In contrast, `ValidationReportColumn` does not implement a custom equals method, meaning two instances with the same label and
 * property are still considered two different instances.
 */
public record ValidationReportColumnKey(String label, String property) {
    public static ValidationReportColumnKey of(ValidationReportColumn column) {
        return new ValidationReportColumnKey(column.getLabel(), column.getProperty());
    }
}
