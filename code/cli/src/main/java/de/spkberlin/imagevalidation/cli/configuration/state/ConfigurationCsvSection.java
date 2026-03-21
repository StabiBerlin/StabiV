package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.cli.output.csv.CsvPropertyColumn;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationCsvSection {

    private String path;
    private List<CsvPropertyColumn> columns;

    public ConfigurationCsvSection() {
        this.columns = new ArrayList<>();
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<CsvPropertyColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<CsvPropertyColumn> columns) {
        this.columns = columns;
    }
}
