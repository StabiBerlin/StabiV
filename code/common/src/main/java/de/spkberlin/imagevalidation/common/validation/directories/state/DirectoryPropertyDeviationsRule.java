package de.spkberlin.imagevalidation.common.validation.directories.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class DirectoryPropertyDeviationsRule {

    @NotBlank
    private String label;

    @NotBlank
    private String property;

    @Valid
    private DirectoryPropertyDeviationsCondition condition;

    private List<@Valid DirectoryPropertyDeviationsSliceEntry> additionalSlices;

    /**
     * Intiailize property deviations rule.
     *
     * @param label a short label for a property that is included in error messages
     * @param property the property that is checked as json path
     * @param condition the deviation confition that is checked
     * @param additionalSlices a list of slice definitions to check a specific range of images for deviations
     */
    public DirectoryPropertyDeviationsRule(
        String label,
        String property,
        DirectoryPropertyDeviationsCondition condition,
        List<DirectoryPropertyDeviationsSliceEntry> additionalSlices
    ) {
        this.label = label;
        this.property = property;
        this.condition = condition;
        this.additionalSlices = additionalSlices;
    }

    /**
     * Intiailize property deviations rule with default settings.
     */
    public DirectoryPropertyDeviationsRule() {
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
