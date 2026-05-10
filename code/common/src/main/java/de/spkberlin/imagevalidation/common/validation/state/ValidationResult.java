package de.spkberlin.imagevalidation.common.validation.state;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ValidationResult {

    private List<String> errors;
    private List<String> warnings;

    public ValidationResult(List<String> errors, List<String> warnings) {
        this.errors = Collections.unmodifiableList(errors);
        this.warnings = Collections.unmodifiableList(warnings);
    }

    public static ValidationResult fromSuccess() {
        return new ValidationResult(Collections.emptyList(), Collections.emptyList());
    }

    public static ValidationResult of(List<String> errors, List<String> warnings) {
        return new ValidationResult(errors, warnings);
    }

    public static ValidationResult of(Collection<ValidationResult> results) {
        return new ValidationResult(
            results.stream().map(ValidationResult::getErrors).flatMap(List::stream).toList(),
            results.stream().map(ValidationResult::getWarnings).flatMap(List::stream).toList()
        );
    }

    public static ValidationResult ofFirst(Collection<ValidationResult> results) {
        return new ValidationResult(
            results.stream().map(ValidationResult::getErrors).flatMap(List::stream).findFirst().stream().toList(),
            results.stream().map(ValidationResult::getWarnings).flatMap(List::stream).findFirst().stream().toList()
        );
    }

    public static ValidationResult of(String message, ValidationFailureSeverity severity) {
        if (ValidationFailureSeverity.error.equals(severity)) {
            return ValidationResult.fromError(message);
        } else {
            return ValidationResult.fromWarning(message);
        }
    }

    public static ValidationResult fromError(String message) {
        if (Objects.nonNull(message) && !message.isBlank()) {
            return new ValidationResult(Collections.singletonList(message), Collections.emptyList());
        }
        return ValidationResult.fromSuccess();
    }

    public static ValidationResult fromWarning(String message) {
        if (Objects.nonNull(message) && !message.isBlank()) {
            return new ValidationResult(Collections.emptyList(), Collections.singletonList(message));
        }
        return ValidationResult.fromSuccess();
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public List<String> getWarnings() {
        return this.warnings;
    }
}
