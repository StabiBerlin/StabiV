package de.spkberlin.imagevalidation.cli.configuration.state;

import jakarta.validation.constraints.NotNull;

public class ConfigurationExcelStyleElement {

    @NotNull
    private Boolean bold;

    private String backgroundColor;
    private String borderColor;

    public ConfigurationExcelStyleElement() {
        this.bold = false;
    }

    /**
     * Initialize excel style element with default values.
     *
     * @param bold whether font is bold or not
     * @param backgroundColor the background color as html hex code
     * @param borderColor the border color as html hex code
     */
    public ConfigurationExcelStyleElement(Boolean bold, String backgroundColor, String borderColor) {
        this.bold = bold;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }

    public Boolean isBold() {
        return this.bold;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public String getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBorderColor() {
        return this.borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
}
