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
import de.spkberlin.imagevalidation.common.validation.images.state.ImageRule;
import de.spkberlin.imagevalidation.common.validation.images.state.ImageValidationResult;
import de.spkberlin.imagevalidation.common.validation.state.ValidationReportColumn;
import de.spkberlin.imagevalidation.common.validation.state.ValidationReportColumnKey;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private static List<String> COLUMN_HEADER = List.of(
        "Path",
        "Directory Name",
        "File Name / Total Images",
        "Directory Passed Validation",
        "File Passed Validation",
        "Validation Error / Warning"
    );

    /**
     * Generate the validation report.
     *
     * @param validateAllResult validation results for all files and directories
     * @param imageRules the image rule configuration used to match rule validation results with property columns
     * @param extraPropertyColumns additional properties to include in the CSV file
     */
    public static List<ReportRow> generateReport(
        ValidateAllResult validateAllResult,
        List<ImageRule> imageRules,
        List<ValidationReportColumn> extraPropertyColumns
    ) {
        logger.info("generate report");
        List<ValidationReportColumnKey> propertyColumnKeys = findAllReportColumns(imageRules, extraPropertyColumns);

        return Stream.concat(
            generateHeader(propertyColumnKeys),
            Stream.concat(writeSummaryLine(validateAllResult), writeDirectories(validateAllResult, propertyColumnKeys, imageRules))
        ).toList();
    }

    private static Stream<ReportRow> writeSummaryLine(ValidateAllResult validateAllResult) {
        Boolean allValid = ValidateAll.isOverallValid(validateAllResult);
        Integer totalFiles = ValidateAll.countOverallImages(validateAllResult);
        ReportCellHighlightType highlightType = allValid ? ReportCellHighlightType.none : ReportCellHighlightType.error;

        return Stream.of(
            ReportRow.ofSummary(
                Stream.of(
                    ReportCell.empty(), // path
                    ReportCell.of("Overall Result"), // directory name
                    ReportCell.of(totalFiles), // file name / total images
                    ReportCell.of(allValid, highlightType), // directory passed validation
                    ReportCell.of(allValid, highlightType), // file passed validation
                    ReportCell.empty() // validation error or warning
                ).toList()
            ),
            ReportRow.empty() // empty record for spacing
        );
    }

    private static Stream<ReportRow> writeDirectories(
        ValidateAllResult validateAllResult,
        List<ValidationReportColumnKey> propertyColumnKeys,
        List<ImageRule> imageRules
    ) {
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
                            .flatMap(filepath ->
                                writeFile(
                                    filepath,
                                    validateAllResult.getImageValidationResults().get(filepath),
                                    propertyColumnKeys,
                                    imageRules
                                )
                            )
                    ),
                    Stream.of(ReportRow.empty())
                );
            });
    }

    private static Stream<ReportRow> writeDirectorySummary(Path directory, ValidateAllResult validateAllResult) {
        DirectoryValidationResult directoryResult = validateAllResult.getDirectoryValidationResults().get(directory);
        boolean passedValidation = DirectoryValidation.isDirectoryValid(directoryResult);
        List<String> directoryErrors = directoryResult.getDirectoryResult().getErrors();
        List<String> directoryWarnings = directoryResult.getDirectoryResult().getWarnings();
        ReportCellHighlightType highlightType = ReportCellHighlightType.fromErrorsAndWarnings(directoryErrors, directoryWarnings);

        // write directory validation result
        return Stream.of(
            ReportRow.ofDirectory(
                Stream.of(
                    ReportCell.of(directory.toString()), // path
                    ReportCell.of(directory.getFileName().toString(), highlightType), // directory name
                    ReportCell.of(directoryResult.getImageResults().size(), highlightType), // file name / total images
                    ReportCell.of(passedValidation, highlightType), // directory passed
                    ReportCell.empty(highlightType),
                    ReportCell.of(directoryErrors, directoryWarnings) // validation error or warning
                ).toList(),
                passedValidation
            )
        );
    }

    private static Stream<ReportRow> writeFile(
        Path filepath,
        ImageValidationResult imageValidationResult,
        List<ValidationReportColumnKey> propertyColumnKeys,
        List<ImageRule> imageRules
    ) {
        Boolean passedValidation = ImageValidation.isImageValid(imageValidationResult);
        List<String> validationErrors = ImageValidation.getAllErrors(imageValidationResult);
        List<String> validationWarnings = ImageValidation.getAllWarnings(imageValidationResult);
        ReportCellHighlightType highlightType = ReportCellHighlightType.fromErrorsAndWarnings(validationErrors, validationWarnings);
        Map<ValidationReportColumnKey, ValidationResult> columnResults = getReportColumnsWithValidationResult(
            imageRules,
            imageValidationResult
        );

        return Stream.of(
            ReportRow.ofImage(
                Stream.concat(
                    Stream.concat(
                        Stream.of(
                            ReportCell.of(filepath.toString()), // path
                            ReportCell.of(filepath.getParent().getFileName().toString()), // directory name
                            ReportCell.of(filepath.getFileName().toString(), highlightType), // file name
                            ReportCell.empty(highlightType), // directory passed validation
                            ReportCell.of(passedValidation, highlightType), // file passed validation
                            ReportCell.of(validationErrors, validationWarnings) // validation error or warning
                        ),
                        propertyColumnKeys
                            .stream()
                            .map(propertyColumnKey ->
                                ReportCell.of(
                                    extractPropertyOrNull(propertyColumnKey.property(), imageValidationResult.getExtractionResult()),
                                    ReportCellHighlightType.fromValidationResult(
                                        columnResults.getOrDefault(propertyColumnKey, ValidationResult.fromSuccess())
                                    )
                                )
                            )
                    ),
                    Stream.of(ReportCell.of(imageValidationResult.getChecksum())) // checksum
                ).toList(),
                passedValidation
            )
        );
    }

    private static Stream<ReportRow> generateHeader(List<ValidationReportColumnKey> propertyColumnKeys) {
        return Stream.of(
            ReportRow.ofHeader(
                Stream.concat(
                    Stream.concat(
                        COLUMN_HEADER.stream().map(ReportCell::of),
                        propertyColumnKeys.stream().map(c -> ReportCell.of(c.label()))
                    ),
                    Stream.of(ReportCell.of("Checksum"))
                ).toList()
            )
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

    private static List<ValidationReportColumnKey> findAllReportColumns(List<ImageRule> rules, List<ValidationReportColumn> extraColumns) {
        Set<ValidationReportColumnKey> keySet = new LinkedHashSet<>();

        for (ImageRule rule : rules) {
            for (ValidationReportColumn column : rule.getReportColumns()) {
                keySet.add(ValidationReportColumnKey.of(column));
            }
        }

        for (ValidationReportColumn column : extraColumns) {
            keySet.add(ValidationReportColumnKey.of(column));
        }

        return new ArrayList<>(keySet);
    }

    private static Map<ValidationReportColumnKey, ValidationResult> getReportColumnsWithValidationResult(
        List<ImageRule> rules,
        ImageValidationResult imageValidationResult
    ) {
        List<ValidationResult> ruleResults = imageValidationResult.getImageRuleEvaluationResults();
        if (rules.size() != ruleResults.size()) {
            throw new IllegalArgumentException("number of rules does not match number of rule validation results");
        }

        Map<ValidationReportColumnKey, ValidationResult> results = new LinkedHashMap<>();

        int i = 0;
        for (ImageRule rule : rules) {
            for (ValidationReportColumn column : rule.getReportColumns()) {
                ValidationReportColumnKey columnKey = ValidationReportColumnKey.of(column);
                if (!results.containsKey(columnKey)) {
                    // validation result
                    results.put(columnKey, ruleResults.get(i));
                } else {
                    // add validation results together (if multiple rules list the same report column as relevant column)
                    results.put(columnKey, ValidationResult.of(List.of(ruleResults.get(i), results.get(columnKey))));
                }
            }

            i += 1;
        }

        return results;
    }
}
