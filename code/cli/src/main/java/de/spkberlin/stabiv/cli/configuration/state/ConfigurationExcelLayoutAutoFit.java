package de.spkberlin.stabiv.cli.configuration.state;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class ConfigurationExcelLayoutAutoFit {

    @NotNull
    private Boolean enabled;

    @PositiveOrZero
    private Integer padding;

    public ConfigurationExcelLayoutAutoFit() {
        this.enabled = true;
        this.padding = 512;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPadding() {
        return this.padding;
    }

    public void setPadding(Integer padding) {
        this.padding = padding;
    }
}
