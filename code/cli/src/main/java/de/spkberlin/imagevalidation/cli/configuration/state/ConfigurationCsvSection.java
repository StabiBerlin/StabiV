package de.spkberlin.imagevalidation.cli.configuration.state;

import de.spkberlin.imagevalidation.cli.output.csv.CsvPropertyColumn;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationCsvSection {

    @Valid
    private ConfigurationPath path;

    private List<@Valid CsvPropertyColumn> columns;

    public ConfigurationCsvSection() {
        this.columns = new ArrayList<>();
    }

    public ConfigurationPath getPath() {
        return this.path;
    }

    public void setPath(ConfigurationPath path) {
        this.path = path;
    }

    public List<CsvPropertyColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<CsvPropertyColumn> columns) {
        this.columns = columns;
    }
}
