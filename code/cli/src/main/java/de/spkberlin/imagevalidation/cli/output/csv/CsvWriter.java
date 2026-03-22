package de.spkberlin.imagevalidation.cli.output.csv;

import com.jayway.jsonpath.JsonPath;
import de.spkberlin.imagevalidation.cli.validation.ImageValidationResult;
import de.spkberlin.imagevalidation.cli.validation.ValidateAllResult;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvWriter {

    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    private static String[] COLUMN_HEADER = {
        "Directory",
        "File",
        "Checksum",
        "Passed Validation",
        "Validation Error",
        "Validation Warning",
    };

    public static void writeReport(Path outputPath, ValidateAllResult validateAllResult, List<CsvPropertyColumn> columns) {
        logger.info("write csv output file '{}'", outputPath);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), CSVFormat.EXCEL)) {
            writeHeader(printer, columns);
            writeDirectories(printer, validateAllResult, columns);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeDirectories(CSVPrinter printer, ValidateAllResult validateAllResult, List<CsvPropertyColumn> columns)
        throws IOException {
        for (Entry<Path, List<Path>> entry : validateAllResult.getFilesByDirectory().entrySet()) {
            Path directory = entry.getKey();
            printer.printRecord(directory.toString());

            for (Path filepath : entry.getValue()) {
                writeFile(printer, filepath, validateAllResult.getImageValidationResults().get(filepath), columns);
            }
        }
    }

    public static void writeFile(
        CSVPrinter printer,
        Path filepath,
        ImageValidationResult imageValidationResult,
        List<CsvPropertyColumn> columns
    ) throws IOException {
        Stream<String> line = Stream.concat(
            Stream.of(
                (String) null, // directory name
                filepath.getFileName().toString(),
                imageValidationResult.getChecksum(),
                imageValidationResult.getImageRuleEvaluationResult().getErrors().isEmpty() ? "true" : "false",
                imageValidationResult.getImageRuleEvaluationResult().getErrors().stream().findFirst().orElse(""),
                imageValidationResult.getImageRuleEvaluationResult().getWarnings().stream().findFirst().orElse("")
            ),
            columns
                .stream()
                .map(column -> extractPropertyOrNull(column.getProperty(), imageValidationResult.getExtractionResult().getProperties()))
        );

        printer.printRecord(line);
    }

    public static void writeHeader(CSVPrinter printer, List<CsvPropertyColumn> columns) throws IOException {
        printer.printRecord(Stream.concat(Stream.of(COLUMN_HEADER), columns.stream().map(c -> c.getLabel())));
    }

    public static String extractPropertyOrNull(String propertyPath, Map<String, Object> properties) {
        Object property = JsonPath.read(properties, propertyPath);
        return PropertiesSerializer.toPrettyString(property);
    }
}
