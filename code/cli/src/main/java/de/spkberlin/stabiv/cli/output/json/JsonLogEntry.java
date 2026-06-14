package de.spkberlin.stabiv.cli.output.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "time", "type", "path", "checksum", "validation", "error", "warning" })
public class JsonLogEntry {

    public static final String FILE_TYPE = "file";
    public static final String DIRECTORY_TYPE = "directory";
    public static final String VALIDATION_SUCCESS = "success";
    public static final String VALIDATION_FAILED = "failed";

    private String time;
    private String type;
    private String path;
    private String checksum;
    private String validation;
    private String error;
    private String warning;

    public JsonLogEntry() {
        // empty
    }

    /**
     * Initialize JSON log entry with parameters.
     *
     * @param time the current time
     * @param type either "file" or "directory"
     * @param path the filepath to the validated file or directory
     * @param checksum the checksum of the validated file, or null if directory
     * @param validation either "success" or "failed" depending on whether validation did succeed
     * @param error the validation error or null
     * @param warning the validation warning or null
     */
    public JsonLogEntry(String time, String type, String path, String checksum, String validation, String error, String warning) {
        this.time = time;
        this.type = type;
        this.path = path;
        this.checksum = checksum;
        this.validation = validation;
        this.error = error;
        this.warning = warning;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getValidation() {
        return this.validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getWarning() {
        return this.warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
