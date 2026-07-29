package de.spkberlin.stabiv.cli.output.report;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.text.StringSubstitutor;

public class ReportFile {

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH-mm-ss");

    /**
     * Resolves a filepath template by substituting variables that may be included in it.
     *
     * @param filepathTemplate the filepath template with substitution variables
     * @param inputBaseDirectory the base directory of the input configuration
     * @return the resolved filepath
     */
    public static String resolveFilepathTemplate(String filepathTemplate, Path inputBaseDirectory) {
        Map<String, String> variables = Map.ofEntries(
            Map.entry("date", LocalDateTime.now().format(dateFormat)),
            Map.entry("time", LocalDateTime.now().format(timeFormat)),
            Map.entry("inputBaseDirectoryName", Objects.toString(inputBaseDirectory.getFileName(), ""))
        );
        return StringSubstitutor.replace(filepathTemplate, variables);
    }
}
