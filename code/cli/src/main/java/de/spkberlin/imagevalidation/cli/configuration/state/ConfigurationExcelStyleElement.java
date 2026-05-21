package de.spkberlin.imagevalidation.cli.configuration.state;

public class ConfigurationExcelStyleElement {

    private boolean bold;
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
    public ConfigurationExcelStyleElement(boolean bold, String backgroundColor, String borderColor) {
        this.bold = bold;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }

    public boolean isBold() {
        return this.bold;
    }

    public void setBold(boolean bold) {
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
