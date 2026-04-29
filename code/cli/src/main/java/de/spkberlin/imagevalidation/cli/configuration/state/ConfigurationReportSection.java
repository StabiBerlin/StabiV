package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.cli.output.report.ReportPropertyColumn;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationReportSection {

    @Valid
    private ConfigurationPath csvPath;

    @Valid
    private ConfigurationPath excelPath;

    private List<@Valid ReportPropertyColumn> columns;

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

    public List<ReportPropertyColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<ReportPropertyColumn> columns) {
        this.columns = columns;
    }
}
