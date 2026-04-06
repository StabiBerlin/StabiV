package de.spkberlin.imagevalidation.cli.output.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.spkberlin.imagevalidation.cli.validation.DirectoryValidationResult;
import de.spkberlin.imagevalidation.cli.validation.ImageValidationResult;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLogWriter {

    private static final Logger logger = LoggerFactory.getLogger(JsonLogWriter.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonPropertyOrder({ "time", "type", "path", "checksum", "validation", "error", "warning" })
    private static record LogEntry(
        String time,
        String type,
        String path,
        String checksum,
        String validation,
        String error,
        String warning
    ) {}

    /**
     * Write a JSON log entry to stdout summarizing the validation result for a single image file.
     *
     * @param result the image validation result for one image file
     */
    public static void writeFileValidationLog(ImageValidationResult result) {
        try {
            String time = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());
            Boolean passed = result.getImageRuleEvaluationResult().getErrors().isEmpty();

            LogEntry entry = new LogEntry(
                time,
                "file",
                result.getFilePath().toString(),
                result.getChecksum(),
                passed ? "success" : "failed",
                result.getImageRuleEvaluationResult().getErrors().stream().findFirst().orElse(null),
                result.getImageRuleEvaluationResult().getWarnings().stream().findFirst().orElse(null)
            );

            System.out.println(mapper.writeValueAsString(entry));
            System.out.flush();
        } catch (IOException e) {
            logger.error("exception writing image validation result as json to stdout");
        }
    }

    /**
     * Write a JSON log entry to stdout summarizing the validation result for a single directory.
     *
     * @param result the validation result for a single image directory
     */
    public static void writeDirectoryValidationLog(DirectoryValidationResult result) {
        try {
            String time = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());

            LogEntry entry = new LogEntry(
                time,
                "directory",
                result.getDirectory().toString(),
                null,
                Objects.isNull(result.getError()) ? "success" : "failed",
                result.getError(),
                null
            );

            System.out.println(mapper.writeValueAsString(entry));
            System.out.flush();
        } catch (IOException e) {
            logger.error("exception writing directory validation result as json to stdout");
        }
    }
}
