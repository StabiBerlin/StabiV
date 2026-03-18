package de.spkberlin.imagevalidation.cli.output.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class JsonLogWriter {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonPropertyOrder({ "time", "type", "path", "validation" })
    static record LogEntry(String time, String type, String path, String validation, String error) {}

    public static void writeFileValidationLog(Path filepath, boolean validationSuccess) throws IOException {
        String time = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());

        LogEntry entry = new LogEntry(time, "file", filepath.toString(), validationSuccess ? "success" : "failed", null);

        System.out.println(mapper.writeValueAsString(entry));
        System.out.flush();
    }

    public static void writeDirectoryValidationLog(Path directory, boolean validationSuccess) throws IOException {
        String time = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());

        LogEntry entry = new LogEntry(time, "directory", directory.toString(), validationSuccess ? "success" : "failed", null);

        System.out.println(mapper.writeValueAsString(entry));
        System.out.flush();
    }
}
