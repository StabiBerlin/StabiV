package de.spkberlin.imagevalidation.common.validation.directories.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationSeverityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DirectoryPropertyDeviationsSliceEntry {

    @NotNull
    @Min(0)
    private Integer from;

    @NotNull
    @Min(0)
    private Integer to;

    @NotNull
    private Boolean reversed;

    @Valid
    private DirectoryPropertyDeviationsCondition condition;

    public DirectoryPropertyDeviationsSliceEntry() {
        this.reversed = false;
    }

    public Integer getFrom() {
        return this.from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return this.to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Boolean isReversed() {
        return this.reversed;
    }

    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }

    public DirectoryPropertyDeviationsCondition getCondition() {
        return this.condition;
    }

    public void setCondition(DirectoryPropertyDeviationsCondition condition) {
        this.condition = condition;
    }
}
