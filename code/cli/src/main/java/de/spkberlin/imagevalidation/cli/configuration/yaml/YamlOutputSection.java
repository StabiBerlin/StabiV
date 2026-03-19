package de.spkberlin.imagevalidation.cli.configuration.yaml;

public class YamlOutputSection {

    private YamlCsvSection csv;
    private YamlLoggingSection logging;

    public YamlOutputSection() {
        csv = new YamlCsvSection();
        logging = new YamlLoggingSection();
    }

    public YamlCsvSection getCsv() {
        return this.csv;
    }

    public void setIncludes(YamlCsvSection csv) {
        this.csv = csv;
    }

    public YamlLoggingSection getLogging() {
        return this.logging;
    }

    public void setLogging(YamlLoggingSection logging) {
        this.logging = logging;
    }
}
