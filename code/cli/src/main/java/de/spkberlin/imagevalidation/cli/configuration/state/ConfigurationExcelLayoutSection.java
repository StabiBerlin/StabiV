package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;
import java.util.List;

public class ConfigurationExcelLayoutSection {

    @Valid
    private ConfigurationExcelLayoutAutoFit autoFitColumns;

    @Valid
    private ConfigurationExcelLayoutFixed fixed;

    private List<Integer> hiddenColumns;

    private boolean groupDirectories;

    private boolean collapseValidDirectories;

    /**
     * Initialize layout section with default values.
     */
    public ConfigurationExcelLayoutSection() {
        this.autoFitColumns = new ConfigurationExcelLayoutAutoFit();
        this.fixed = new ConfigurationExcelLayoutFixed();
        this.hiddenColumns = List.of(0);
        this.groupDirectories = true;
        this.collapseValidDirectories = true;
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

    public void setGroupDirectories(boolean groupDirectories) {
        this.groupDirectories = groupDirectories;
    }

    public boolean isCollapseValidDirectories() {
        return collapseValidDirectories;
    }

    public void setCollapseValidDirectories(boolean filterValidImages) {
        this.collapseValidDirectories = filterValidImages;
    }
}
