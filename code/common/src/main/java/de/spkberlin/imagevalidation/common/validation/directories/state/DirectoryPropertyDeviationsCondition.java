package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationSeverityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DirectoryPropertyDeviationsCondition {

    @Valid
    private DirectoryPropertyDeviationsAggregateType aggregate;

    @NotNull
    private Double maxDeviation;

    @NotNull
    private Boolean inPercent;

    @Valid
    private ValidationSeverityType severity;

    /**
     * Initialize property deviations condition with default values.
     */
    public DirectoryPropertyDeviationsCondition() {
        this.inPercent = false;
        this.severity = ValidationSeverityType.error;
    }

    /**
     * Initialize property deviations condition.
     *
     * @param aggregate the aggregation function that is applied (either average or median)
     * @param maxDeviation the maximum allowed deviation
     * @param inPercent whether the maximum allowed deviation is interpreted as percent or absolute value
     */
    public DirectoryPropertyDeviationsCondition(
        DirectoryPropertyDeviationsAggregateType aggregate,
        Double maxDeviation,
        Boolean inPercent
    ) {
        this.aggregate = aggregate;
        this.maxDeviation = maxDeviation;
        this.inPercent = inPercent;
    }

    public Double getMaxDeviation() {
        return this.maxDeviation;
    }

    public void setMaxDeviation(Double maxDeviation) {
        this.maxDeviation = maxDeviation;
    }

    public Boolean isInPercent() {
        return this.inPercent;
    }

    public void setInPercent(Boolean inPercent) {
        this.inPercent = inPercent;
    }

    public DirectoryPropertyDeviationsAggregateType getAggregate() {
        return this.aggregate;
    }

    public void setAggregate(DirectoryPropertyDeviationsAggregateType aggregate) {
        this.aggregate = aggregate;
    }

    public ValidationSeverityType getSeverity() {
        return this.severity;
    }

    public void setSeverity(ValidationSeverityType severity) {
        this.severity = severity;
    }
}
