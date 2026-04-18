package de.spkberlin.imagevalidation.common.validation.directories.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class DirectoryPropertyDeviationsEntry {

    @NotBlank
    private String label;

    @NotBlank
    private String property;

    @Valid
    private DirectoryPropertyDeviationsCondition condition;

    private List<@Valid DirectoryPropertyDeviationsSliceEntry> additionalSlices;

    public DirectoryPropertyDeviationsEntry() {
        additionalSlices = new ArrayList<>();
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public DirectoryPropertyDeviationsCondition getCondition() {
        return this.condition;
    }

    public void setCondition(DirectoryPropertyDeviationsCondition condition) {
        this.condition = condition;
    }

    public List<DirectoryPropertyDeviationsSliceEntry> getAdditionalSlices() {
        return this.additionalSlices;
    }

    public void setAdditionalSlices(List<DirectoryPropertyDeviationsSliceEntry> additionalSlices) {
        this.additionalSlices = additionalSlices;
    }
}
