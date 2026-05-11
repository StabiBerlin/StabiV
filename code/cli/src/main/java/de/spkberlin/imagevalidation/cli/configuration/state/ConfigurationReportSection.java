package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationReportColumn;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationReportSection {

    @Valid
    private ConfigurationPath csvPath;

    @Valid
    private ConfigurationPath excelPath;

    private List<@Valid ValidationReportColumn> columns;

    public ConfigurationReportSection() {
        this.columns = new ArrayList<>();
    }

    public ConfigurationPath getCsvPath() {
        return this.csvPath;
    }

    public void setCsvPath(ConfigurationPath csvPath) {
        this.csvPath = csvPath;
    }

    public ConfigurationPath getExcelPath() {
        return this.excelPath;
    }

    public void setExcelPath(ConfigurationPath excelPath) {
        this.excelPath = excelPath;
    }

    public List<ValidationReportColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<ValidationReportColumn> columns) {
        this.columns = columns;
    }
}
