package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.constraints.PositiveOrZero;

public class ConfigurationExcelLayoutFixed {

    @PositiveOrZero
    private Integer rows;

    @PositiveOrZero
    private Integer columns;

    public ConfigurationExcelLayoutFixed() {
        this.rows = 2;
        this.columns = 7;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getColumns() {
        return this.columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }
}
