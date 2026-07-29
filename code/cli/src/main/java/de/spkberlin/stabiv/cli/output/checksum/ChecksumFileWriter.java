package de.spkberlin.stabiv.cli.output.checksum;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecksumFileWriter {

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH-mm-ss");

    private static final Logger logger = LoggerFactory.getLogger(ChecksumFileWriter.class);

    /**
     * Write a checksum file to a directory assuming that the directory was succesfully validated.
     *
     * @param directory the directory that was succesfully validated
     * @param filenameTemplate the filename template used to generate a filename
     * @param lineFormatTemplate the line format template used to generate lines of the checksum file
     * @param checksums the checksums for each validated file in the directory
     * @throws IOException in case writing the checksum file fails
     */
    public static void writeFile(Path directory, String filenameTemplate, String lineFormatTemplate, Map<Path, String> checksums)
        throws IOException {
        if (Objects.isNull(filenameTemplate) || filenameTemplate.isBlank()) {
            logger.trace("skip writing checksum file because filename pattern is not defined");
            return;
        }

        if (Objects.isNull(lineFormatTemplate) || lineFormatTemplate.isBlank()) {
            logger.trace("skip writing checksum file because line format is not defined");
            return;
        }

        String resolvedFilename = resolveFilenameTemplate(filenameTemplate, directory);
        Path checksumFilepath = directory.resolve(resolvedFilename);
        String checksumFileContent = checksums
            .entrySet()
            .stream()
            .filter(e -> Objects.nonNull(e.getKey()) && Objects.nonNull(e.getValue()))
            .sorted(Map.Entry.comparingByKey())
            .map(e -> resolveLineFormat(lineFormatTemplate, e.getValue(), Objects.toString(e.getKey().getFileName(), "")))
            .collect(Collectors.joining("\n"));

        logger.info("write checksum file '{}'", checksumFilepath.toString());
        Files.writeString(
            checksumFilepath,
            checksumFileContent,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private static String resolveFilenameTemplate(String filenameTemplate, Path directory) {
        Map<String, String> variables = Map.ofEntries(
            Map.entry("directory", Objects.toString(directory.getFileName(), "")),
            Map.entry("date", LocalDateTime.now().format(dateFormat)),
            Map.entry("time", LocalDateTime.now().format(timeFormat))
        );
        return StringSubstitutor.replace(filenameTemplate, variables);
    }

    private static String resolveLineFormat(String lineFormatTemplaet, String checksum, String imageFilename) {
        Map<String, String> variables = Map.ofEntries(Map.entry("checksum", checksum), Map.entry("filename", imageFilename));
        return StringSubstitutor.replace(lineFormatTemplaet, variables);
    }
}
