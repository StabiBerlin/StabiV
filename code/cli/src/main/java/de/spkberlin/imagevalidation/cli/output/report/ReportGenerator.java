package de.spkberlin.imagevalidation.cli.output.report;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.cli.validation.ValidateAll;
import de.spkberlin.imagevalidation.cli.validation.ValidateAllResult;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import de.spkberlin.imagevalidation.common.validation.directories.DirectoryValidation;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryValidationResult;
import de.spkberlin.imagevalidation.common.validation.images.ImageValidation;
import de.spkberlin.imagevalidation.common.validation.images.state.ImageValidationResult;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private static List<String> COLUMN_HEADER = List.of(
        "Heading",
        "Subheading",
        "Directory Name",
        "File Name",
        "Directory Passed Validation",
        "File Passed Validation",
        "Validation Error or Warning",
        "Path",
        "Checksum"
    );

    /**
     * Generate the validation report.
     *
     * @param validateAllResult validation results for all files and directories
     * @param propertyColumns additional properties to include in the CSV file
     */
    public static List<List<ReportCell>> generateReport(ValidateAllResult validateAllResult, List<ReportPropertyColumn> propertyColumns) {
        logger.info("generate report");

        return Stream.concat(
            generateHeader(propertyColumns),
            Stream.concat(writeSummaryLine(validateAllResult), writeDirectories(validateAllResult, propertyColumns))
        )
            .map(s -> s.toList())
            .toList();
    }

    private static Stream<Stream<ReportCell>> writeSummaryLine(ValidateAllResult validateAllResult) {
        Boolean allValid = ValidateAll.isOverallValid(validateAllResult);
        Integer totalFiles = ValidateAll.countOverallImages(validateAllResult);
        Long validFiles = ValidateAll.countValidImages(validateAllResult);

        return Stream.of(
            Stream.empty(), // empty record for spacing
            Stream.of(
                "Overall Result", // heading
                "Target vs. Actual", // subheading
                totalFiles.toString(), // directory name
                validFiles.toString(), // file name
                allValid.toString(), // directory passed validation
                allValid.toString(), // file passed validation
                null, // validation error or warning
                null, // path
                null // checksum
            ).map(ReportCell::of),
            Stream.empty() // empty record for spacing
        );
    }

    private static Stream<Stream<ReportCell>> writeDirectories(ValidateAllResult validateAllResult, List<ReportPropertyColumn> columns) {
        return validateAllResult
            .getFilesByDirectory()
            .entrySet()
            .stream()
            .flatMap(entry -> {
                return Stream.concat(
                    Stream.concat(
                        writeDirectorySummary(entry.getKey(), validateAllResult),
                        entry
                            .getValue()
                            .stream()
                            .flatMap(filepath -> writeFile(filepath, validateAllResult.getImageValidationResults().get(filepath), columns))
                    ),
                    Stream.of(Stream.empty())
                );
            });
    }

    private static Stream<Stream<ReportCell>> writeDirectorySummary(Path directory, ValidateAllResult validateAllResult) {
        DirectoryValidationResult directoryResult = validateAllResult.getDirectoryValidationResults().get(directory);
        boolean passedValidation = DirectoryValidation.isDirectoryValid(directoryResult);
        List<String> directoryErrors = directoryResult.getDirectoryResult().getErrors();
        List<String> directoryWarnings = directoryResult.getDirectoryResult().getWarnings();
        Long validImageCount = DirectoryValidation.countValidImageForDirectory(directoryResult);

        // write directory validation result
        return Stream.of(
            Stream.of(
                ReportCell.of(directory.getFileName().toString()), // heading
                ReportCell.of("Target vs. Actual"), // subheading
                ReportCell.of(String.valueOf(validateAllResult.getFilesByDirectory().get(directory).size())), // direcotry name
                ReportCell.of(String.valueOf(validImageCount)), // file name
                ReportCell.of(passedValidation), // directory passed
                ReportCell.empty(), // file passed validation
                ReportCell.of(directoryErrors, directoryWarnings), // validation error or warning
                ReportCell.of(directory.toString()), // path
                ReportCell.empty() // checksum
            )
        );
    }

    private static Stream<Stream<ReportCell>> writeFile(
        Path filepath,
        ImageValidationResult imageValidationResult,
        List<ReportPropertyColumn> columns
    ) {
        Boolean passedValidation = ImageValidation.isImageValid(imageValidationResult);
        List<String> validationErrors = ImageValidation.getAllErrors(imageValidationResult);
        List<String> validationWarnings = ImageValidation.getAllWarnings(imageValidationResult);

        return Stream.concat(
            Stream.of(
                Stream.concat(
                    Stream.of(
                        ReportCell.empty(), // heading
                        ReportCell.empty(), // subheading
                        ReportCell.of(filepath.getParent().getFileName().toString()), // directory name
                        ReportCell.of(filepath.getFileName().toString()), // file name
                        ReportCell.empty(), // directory passed validation
                        ReportCell.of(passedValidation), // file passed validation
                        ReportCell.of(validationErrors, validationWarnings), // validation error or warning
                        ReportCell.of(filepath.toString()), // file path
                        ReportCell.of(imageValidationResult.getChecksum()) // checksum
                    ),
                    columns
                        .stream()
                        .map(column ->
                            ReportCell.of(extractPropertyOrNull(column.getProperty(), imageValidationResult.getExtractionResult()))
                        )
                )
            ),
            Stream.empty()
        );
    }

    private static Stream<Stream<ReportCell>> generateHeader(List<ReportPropertyColumn> propertyColumns) {
        return Stream.of(
            Stream.concat(COLUMN_HEADER.stream().map(ReportCell::of), propertyColumns.stream().map(c -> ReportCell.of(c.getLabel())))
        );
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
