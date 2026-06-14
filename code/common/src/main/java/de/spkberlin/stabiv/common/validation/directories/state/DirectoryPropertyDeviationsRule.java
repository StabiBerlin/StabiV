package de.spkberlin.stabiv.common.validation.directories.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class DirectoryPropertyDeviationsRule {

    @NotBlank
    private String label;

    @NotBlank
    private String property;

    @NotNull
    private Boolean reportColumn;

    @Valid
    private DirectoryPropertyDeviationsCondition condition;

    private List<@Valid DirectoryPropertyDeviationsSliceEntry> additionalSlices;

    /**
     * Intiailize property deviations rule.
     *
     * @param label a short label for a property that is included in error messages
     * @param property the property that is checked as json path
     * @param reportColumn whether the property is added as an additional column to the final validation report
     * @param condition the deviation confition that is checked
     * @param additionalSlices a list of slice definitions to check a specific range of images for deviations
     */
    public DirectoryPropertyDeviationsRule(
        String label,
        String property,
        Boolean reportColumn,
        DirectoryPropertyDeviationsCondition condition,
        List<DirectoryPropertyDeviationsSliceEntry> additionalSlices
    ) {
        this.label = label;
        this.property = property;
        this.reportColumn = reportColumn;
        this.condition = condition;
        this.additionalSlices = additionalSlices;
    }

    /**
     * Intiailize property deviations rule with default settings.
     */
    public DirectoryPropertyDeviationsRule() {
        this.label = null;
        this.property = null;
        this.condition = null;
        this.additionalSlices = new ArrayList<>();
        this.reportColumn = true;
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

    public Boolean isReportColumn() {
        return this.reportColumn;
    }

    public void setReportColumn(Boolean reportColumn) {
        this.reportColumn = reportColumn;
    }
}
