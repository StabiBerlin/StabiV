package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.common.validation.state.ValidationReportColumn;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationReportSection {

    @Valid
    private ConfigurationCsvSection csv;

    @Valid
    private ConfigurationExcelSection excel;

    private List<@Valid ValidationReportColumn> columns;

    public ConfigurationReportSection() {
        this.columns = new ArrayList<>();
        this.excel = new ConfigurationExcelSection();
    }

    public ConfigurationCsvSection getCsv() {
        return this.csv;
    }

    public void setCsv(ConfigurationCsvSection csv) {
        this.csv = csv;
    }

    public ConfigurationExcelSection getExcel() {
        return this.excel;
    }

    public void setExcel(ConfigurationExcelSection excel) {
        this.excel = excel;
    }

    public List<ValidationReportColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<ValidationReportColumn> columns) {
        this.columns = columns;
    }
}
