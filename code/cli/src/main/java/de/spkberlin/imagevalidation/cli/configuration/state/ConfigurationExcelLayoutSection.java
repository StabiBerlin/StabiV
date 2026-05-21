package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ConfigurationExcelLayoutSection {

    @Valid
    private ConfigurationExcelLayoutAutoFit autoFitColumns;

    @Valid
    private ConfigurationExcelLayoutFixed fixed;

    private List<Integer> hiddenColumns;

    @NotNull
    private Boolean groupDirectories;

    @NotNull
    private Boolean collapseValidDirectories;

    @NotNull
    private Boolean onlyFirstListEntry;

    /**
     * Initialize layout section with default values.
     */
    public ConfigurationExcelLayoutSection() {
        this.autoFitColumns = new ConfigurationExcelLayoutAutoFit();
        this.fixed = new ConfigurationExcelLayoutFixed();
        this.hiddenColumns = List.of(0);
        this.groupDirectories = true;
        this.collapseValidDirectories = true;
        this.onlyFirstListEntry = false;
    }

    public ConfigurationExcelLayoutAutoFit getAutoFitColumns() {
        return this.autoFitColumns;
    }

    public void setAutoFitColumns(ConfigurationExcelLayoutAutoFit autoFitColumns) {
        this.autoFitColumns = autoFitColumns;
    }

    public ConfigurationExcelLayoutFixed getFixed() {
        return this.fixed;
    }

    public void setFixed(ConfigurationExcelLayoutFixed fixed) {
        this.fixed = fixed;
    }

    public List<Integer> getHiddenColumns() {
        return this.hiddenColumns;
    }

    public void setHiddenColumns(List<Integer> hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    public boolean isGroupDirectories() {
        return this.groupDirectories;
    }

    public void setGroupDirectories(Boolean groupDirectories) {
        this.groupDirectories = groupDirectories;
    }

    public Boolean isCollapseValidDirectories() {
        return collapseValidDirectories;
    }

    public void setCollapseValidDirectories(Boolean filterValidImages) {
        this.collapseValidDirectories = filterValidImages;
    }

    public Boolean isOnlyFirstListEntry() {
        return this.onlyFirstListEntry;
    }

    public void setOnlyFirstListEntry(Boolean onlFirstListEntry) {
        this.onlyFirstListEntry = onlFirstListEntry;
    }
}
