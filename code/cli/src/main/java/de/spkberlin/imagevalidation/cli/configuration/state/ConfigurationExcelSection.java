package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.Valid;

public class ConfigurationExcelSection {

    @Valid
    private ConfigurationPath path;

    @Valid
    private ConfigurationExcelLayoutSection layout;

    @Valid
    private ConfigurationExcelStyleSection style;

    public ConfigurationExcelSection() {
        layout = new ConfigurationExcelLayoutSection();
        style = new ConfigurationExcelStyleSection();
    }

    public ConfigurationPath getPath() {
        return this.path;
    }

    public void setPath(ConfigurationPath path) {
        this.path = path;
    }

    public ConfigurationExcelLayoutSection getLayout() {
        return this.layout;
    }

    public void setLayout(ConfigurationExcelLayoutSection layout) {
        this.layout = layout;
    }

    public ConfigurationExcelStyleSection getStyle() {
        return this.style;
    }

    public void setStyle(ConfigurationExcelStyleSection style) {
        this.style = style;
    }
}
