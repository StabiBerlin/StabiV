package de.spkberlin.imagevalidation.cli.output.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;

public class ReportFile {

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH-mm-ss");

    /**
     * Resolves a filepath template by substituting variables that may be included in it.
     *
     * @param filepathTemplate the filepath template with substitution variables
     * @return the resolved filepath
     */
    public static String resolveFilepathTemplate(String filepathTemplate) {
        Map<String, String> variables = Map.ofEntries(
            Map.entry("date", LocalDateTime.now().format(dateFormat)),
            Map.entry("time", LocalDateTime.now().format(timeFormat))
        );
        return StringSubstitutor.replace(filepathTemplate, variables);
    }
}
