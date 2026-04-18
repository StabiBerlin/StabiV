package de.spkberlin.imagevalidation.common.validation.directories.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DirectoryPropertyDeviationsCondition {

    @Valid
    private DirectoryPropertyDeviationsAggregateType aggregate;

    @NotNull
    private Integer maxDeviation;

    @NotNull
    private Boolean inPercent;

    public DirectoryPropertyDeviationsCondition() {
        this.inPercent = false;
    }

    public Integer getMaxDeviation() {
        return this.maxDeviation;
    }

    public void setMaxDeviation(Integer maxDeviation) {
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
}
