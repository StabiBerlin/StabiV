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

    /**
     * Create a validation result representing validation success (no errors or warnings).
     *
     * @return the validation result
     */
    public static ValidationResult fromSuccess() {
        return new ValidationResult(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Create a validation result with specific errors and warnings.
     *
     * @return the validation result
     */
    public static ValidationResult of(List<String> errors, List<String> warnings) {
        return new ValidationResult(errors, warnings);
    }

    /**
     * Create a validation result with specific errors or warnings depending on severity level.
     *
     * @return the validation result
     */
    public static ValidationResult of(List<String> messages, ValidationFailureSeverity severity) {
        if (ValidationFailureSeverity.error.equals(severity)) {
            return new ValidationResult(messages, Collections.emptyList());
        }
        return new ValidationResult(Collections.emptyList(), messages);
    }

    /**
     * Create a validation result by combining a collection of other validation results, merging all errors and warnings.
     *
     * @return the validation result
     */
    public static ValidationResult of(Collection<ValidationResult> results) {
        return new ValidationResult(
            results.stream().map(ValidationResult::getErrors).flatMap(List::stream).toList(),
            results.stream().map(ValidationResult::getWarnings).flatMap(List::stream).toList()
        );
    }

    /**
     * Create a validation result from a single message with a specific severity level (error or warning).
     *
     * @return the validation result
     */
    public static ValidationResult of(String message, ValidationFailureSeverity severity) {
        if (ValidationFailureSeverity.error.equals(severity)) {
            return ValidationResult.fromError(message);
        } else {
            return ValidationResult.fromWarning(message);
        }
    }

    /**
     * Create a validation result from a collection of other validation results only considering the first error and first warning.
     *
     * @return the validation result
     */
    public static ValidationResult ofFirst(Collection<ValidationResult> results) {
        return new ValidationResult(
            results.stream().map(ValidationResult::getErrors).flatMap(List::stream).findFirst().stream().toList(),
            results.stream().map(ValidationResult::getWarnings).flatMap(List::stream).findFirst().stream().toList()
        );
    }

    /**
     * Create a validation result for a single error message.
     *
     * @return the validation result
     */
    public static ValidationResult fromError(String message) {
        if (Objects.nonNull(message) && !message.isBlank()) {
            return new ValidationResult(Collections.singletonList(message), Collections.emptyList());
        }
        return ValidationResult.fromSuccess();
    }

    /**
     * Create a validation result for a single warning message.
     *
     * @return the validation result
     */
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
