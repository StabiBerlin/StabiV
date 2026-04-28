package de.spkberlin.imagevalidation.cli.output.csv;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.cli.validation.ImageValidationResult;
import de.spkberlin.imagevalidation.cli.validation.ValidateAll;
import de.spkberlin.imagevalidation.cli.validation.ValidateAllResult;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvWriter {

    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    private static String[] COLUMN_HEADER = {
        "Heading",
        "Subheading",
        "Directory Name",
        "File Name",
        "Directory Passed Validation",
        "File Passed Validation",
        "Validation Error",
        "Validation Warning",
        "Path",
        "Checksum",
    };

    /**
     * Write the CSV validation report file.
     *
     * @param outputPath output filepath of the CSV file
     * @param validateAllResult validation results for all files and directories
     * @param columns additional properties to include in the CSV file
     */
    public static void writeReport(Path outputPath, ValidateAllResult validateAllResult, List<CsvPropertyColumn> columns) {
        logger.info("write csv output file '{}'", outputPath);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), CSVFormat.EXCEL)) {
            writeHeader(printer, columns);
            writeSummaryLine(printer, validateAllResult);
            writeDirectories(printer, validateAllResult, columns);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void writeSummaryLine(CSVPrinter printer, ValidateAllResult validateAllResult) throws IOException {
        Boolean allValid = ValidateAll.isOverallValid(validateAllResult);
        Integer totalFiles = ValidateAll.countOverallImages(validateAllResult);
        Long validFiles = ValidateAll.countValidImages(validateAllResult);

        printer.printRecord(); // empty record for spacing

        printer.printRecord(
            "Overall Result", // heading
            "Target vs. Actual", // subheading
            totalFiles, // directory name
            validFiles, // file name
            allValid, // directory passed validation
            allValid, // file passed validation
            null, // validation error
            null, // validation warning
            null, // path
            null // checksum
        );

        printer.printRecord(); // empty record for spacing
    }

    private static void writeDirectories(CSVPrinter printer, ValidateAllResult validateAllResult, List<CsvPropertyColumn> columns)
        throws IOException {
        Iterator<Entry<Path, List<Path>>> entryIterator = validateAllResult.getFilesByDirectory().entrySet().iterator();
        while (entryIterator.hasNext()) {
            Entry<Path, List<Path>> entry = entryIterator.next();
            Path directory = entry.getKey();

            writeDirectorySummary(printer, directory, validateAllResult);

            for (Path filepath : entry.getValue()) {
                writeFile(printer, filepath, validateAllResult.getImageValidationResults().get(filepath), columns);
            }

            if (entryIterator.hasNext()) {
                // one empty line between directories
                printer.printRecord();
            }
        }
    }

    private static void writeDirectorySummary(CSVPrinter printer, Path directory, ValidateAllResult validateAllResult) throws IOException {
        String directoryError = validateAllResult.getDirectoryValidationResults().get(directory).getError();

        // write directory validation result
        printer.printRecord(
            directory.getFileName(), // heading
            "Target vs. Actual", // subheading
            validateAllResult.getFilesByDirectory().get(directory).size(), // direcotry name
            validateAllResult
                .getFilesByDirectory()
                .get(directory)
                .stream()
                .filter(p -> validateAllResult.getImageValidationResults().get(p).getImageRuleEvaluationResult().getErrors().isEmpty())
                .count(), // file name
            Objects.isNull(directoryError), // directory passed validation
            null, // file passed validation
            directoryError, // validation error
            null, // validation warning
            directory.toString(), // path
            null // checksum
        );
    }

    private static void writeFile(
        CSVPrinter printer,
        Path filepath,
        ImageValidationResult imageValidationResult,
        List<CsvPropertyColumn> columns
    ) throws IOException {
        Stream<String> line = Stream.concat(
            Stream.of(
                null, // heading
                null, // subheading
                filepath.getParent().getFileName().toString(), // directory name
                filepath.getFileName().toString(), // file name
                (String) null, // directory passed validation
                imageValidationResult.getImageRuleEvaluationResult().getErrors().isEmpty() ? "true" : "false", // file passed validation
                imageValidationResult.getImageRuleEvaluationResult().getErrors().stream().findFirst().orElse(""), // validation error
                imageValidationResult.getImageRuleEvaluationResult().getWarnings().stream().findFirst().orElse(""), // validation warning
                filepath.toString(), // file path
                imageValidationResult.getChecksum() // checksum
            ),
            columns.stream().map(column -> extractPropertyOrNull(column.getProperty(), imageValidationResult.getExtractionResult()))
        );

        printer.printRecord(line);
    }

    private static void writeHeader(CSVPrinter printer, List<CsvPropertyColumn> columns) throws IOException {
        printer.printRecord(Stream.concat(Stream.of(COLUMN_HEADER), columns.stream().map(c -> c.getLabel())));
    }

    private static String extractPropertyOrNull(String propertyPath, ExtractionResult extractionResult) {
        if (Objects.isNull(extractionResult) || Objects.isNull(extractionResult.getProperties())) {
            return "";
        }
        try {
            Object property = JsonPath.read(extractionResult.getProperties(), propertyPath);
            return PropertiesSerializer.toPrettyString(property);
        } catch (PathNotFoundException e) {
            return "";
        }
    }
}
