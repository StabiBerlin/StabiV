package de.spkberlin.imagevalidation.common.validation.images.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationFailureSeverity;
import de.spkberlin.imagevalidation.common.validation.state.ValidationReportColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class ImageRule {

    private String context;

    @Valid
    private ImageRuleCondition condition;

    @Valid
    private ValidationFailureSeverity severity;

    @NotBlank
    private String message;

    private List<@Valid ValidationReportColumn> reportColumns;

    public ImageRule() {
        reportColumns = new ArrayList<>();
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public ImageRuleCondition getCondition() {
        return this.condition;
    }

    public void setCondition(ImageRuleCondition condition) {
        this.condition = condition;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ValidationFailureSeverity getSeverity() {
        return this.severity;
    }

    public void setSeverity(ValidationFailureSeverity severity) {
        this.severity = severity;
    }

    public List<ValidationReportColumn> getReportColumns() {
        return this.reportColumns;
    }

    public void setReportColumns(List<ValidationReportColumn> reportColumns) {
        this.reportColumns = reportColumns;
    }
}
