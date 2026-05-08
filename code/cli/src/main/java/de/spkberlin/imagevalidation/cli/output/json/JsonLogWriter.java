package de.spkberlin.imagevalidation.cli.output.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.spkberlin.imagevalidation.cli.validation.DirectoryValidationResult;
import de.spkberlin.imagevalidation.cli.validation.ImageValidationResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLogWriter {

    private static final Logger logger = LoggerFactory.getLogger(JsonLogWriter.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Write a JSON log entry to stdout summarizing the validation result for a single image file.
     *
     * @param result the image validation result for one image file
     * @param stdout the stdout print writer to write json messages to
     */
    public static void writeFileValidationLog(ImageValidationResult result, PrintWriter stdout) {
        try {
            String time = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());
            Boolean passed = result.getImageRuleEvaluationResult().getErrors().isEmpty();

            JsonLogEntry entry = new JsonLogEntry(
                time,
                JsonLogEntry.FILE_TYPE,
                result.getFilePath().toString(),
                result.getChecksum(),
                passed ? JsonLogEntry.VALIDATION_SUCCESS : JsonLogEntry.VALIDATION_FAILED,
                result.getImageRuleEvaluationResult().getErrors().stream().findFirst().orElse(null),
                result.getImageRuleEvaluationResult().getWarnings().stream().findFirst().orElse(null)
            );

            stdout.println(mapper.writeValueAsString(entry));
            stdout.flush();
        } catch (IOException e) {
            logger.error("exception writing image validation result as json to stdout");
        }
    }

    /**
     * Write a JSON log entry to stdout summarizing the validation result for a single directory.
     *
     * @param result the validation result for a single image directory
     * @param stdout the stdout print writer to write json messages to
     */
    public static void writeDirectoryValidationLog(DirectoryValidationResult result, PrintWriter stdout) {
        try {
            String time = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());

            JsonLogEntry entry = new JsonLogEntry(
                time,
                JsonLogEntry.DIRECTORY_TYPE,
                result.getDirectory().toString(),
                null,
                result.getErrors().isEmpty() ? JsonLogEntry.VALIDATION_SUCCESS : JsonLogEntry.VALIDATION_FAILED,
                result.getErrors().stream().findFirst().orElse(""),
                null
            );

            stdout.println(mapper.writeValueAsString(entry));
            stdout.flush();
        } catch (IOException e) {
            logger.error("exception writing directory validation result as json to stdout");
        }
    }
}
