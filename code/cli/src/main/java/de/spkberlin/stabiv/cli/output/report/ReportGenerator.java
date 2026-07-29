package de.spkberlin.stabiv.cli.output.report;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.stabiv.cli.common.Constants;
import de.spkberlin.stabiv.cli.validation.ValidateAll;
import de.spkberlin.stabiv.cli.validation.ValidateAllResult;
import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.properties.PropertiesSerializer;
import de.spkberlin.stabiv.common.validation.directories.DirectoryValidation;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsistencyCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryValidationResult;
import de.spkberlin.stabiv.common.validation.images.ImageValidation;
import de.spkberlin.stabiv.common.validation.images.state.ImageRule;
import de.spkberlin.stabiv.common.validation.images.state.ImageValidationResult;
import de.spkberlin.stabiv.common.validation.state.ValidationReportColumn;
import de.spkberlin.stabiv.common.validation.state.ValidationReportColumnKey;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private static List<String> COLUMN_HEADER = List.of(
        "Path",
        "Directory Name",
        "File Name / Total Images",
        "Directory Passed",
        "File Passed",
        "Validation Errors / Warnings",
        "#"
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
        List<DirectoryConsistencyCondition> consistencyRules,
        List<DirectoryPropertyDeviationsRule> deviationRules,
        List<ValidationReportColumn> extraPropertyColumns,
        List<String> commandArgs,
        Path configFilepath,
        Map<String, String> extractorVersions
    ) {
        logger.info("generate report");
        List<ValidationReportColumnKey> propertyColumnKeys = findAllReportColumns(
            imageRules,
            consistencyRules,
            deviationRules,
            extraPropertyColumns
        );

        return Stream.concat(
            generateHeader(propertyColumnKeys),
            Stream.concat(
                writeSummaryLine(validateAllResult),
                Stream.concat(
                    writeDirectories(validateAllResult, propertyColumnKeys, imageRules, consistencyRules, deviationRules),
                    writeMetadata(commandArgs, configFilepath, extractorVersions)
                )
            )
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
                    ReportCell.empty(), // validation error or warning
                    ReportCell.empty() // error count
                ).toList()
            ),
            ReportRow.empty() // empty record for spacing
        );
    }

    private static Stream<ReportRow> writeDirectories(
        ValidateAllResult validateAllResult,
        List<ValidationReportColumnKey> propertyColumnKeys,
        List<ImageRule> imageRules,
        List<DirectoryConsistencyCondition> consistencyRules,
        List<DirectoryPropertyDeviationsRule> deviationRules
    ) {
        return validateAllResult
            .getFilesByDirectory()
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .flatMap(entry -> {
                return Stream.concat(
                    Stream.concat(
                        writeDirectorySummary(entry.getKey(), validateAllResult),
                        entry
                            .getValue()
                            .stream()
                            .sorted()
                            .flatMap(filepath ->
                                writeFile(
                                    filepath,
                                    validateAllResult.getImageValidationResults().get(filepath),
                                    propertyColumnKeys,
                                    imageRules,
                                    consistencyRules,
                                    deviationRules
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
        int errorWarningCount = directoryErrors.size() + directoryWarnings.size();
        Integer expectedNumberOfFiles = directoryResult.getExpectedNumberOfFiles();
        Integer numberOfFilesChecked = Integer.valueOf(directoryResult.getImageResults().size());

        ReportCell totalImagesCell =
            Objects.isNull(expectedNumberOfFiles) || expectedNumberOfFiles.equals(numberOfFilesChecked)
                ? ReportCell.of(numberOfFilesChecked, highlightType)
                : ReportCell.of(
                      String.format("expected %d images, found %d images", expectedNumberOfFiles, numberOfFilesChecked),
                      highlightType
                  );

        // write directory validation result
        return Stream.of(
            ReportRow.ofDirectory(
                Stream.of(
                    ReportCell.of(directory.toAbsolutePath().normalize().toString()), // path
                    ReportCell.of(Objects.toString(directory.getFileName(), ""), highlightType), // directory name
                    totalImagesCell, // file name / total images
                    ReportCell.of(passedValidation, highlightType), // directory passed
                    ReportCell.empty(highlightType),
                    ReportCell.of(directoryErrors, directoryWarnings), // validation error or warning
                    ReportCell.of(errorWarningCount > 0 ? errorWarningCount : null, highlightType)
                ).toList(),
                passedValidation
            )
        );
    }

    private static Stream<ReportRow> writeFile(
        Path filepath,
        ImageValidationResult imageValidationResult,
        List<ValidationReportColumnKey> propertyColumnKeys,
        List<ImageRule> imageRules,
        List<DirectoryConsistencyCondition> consistencyRules,
        List<DirectoryPropertyDeviationsRule> deviationRules
    ) {
        Boolean passedValidation = ImageValidation.isImageValid(imageValidationResult);
        List<String> validationErrors = ImageValidation.getAllErrors(imageValidationResult);
        List<String> validationWarnings = ImageValidation.getAllWarnings(imageValidationResult);
        ReportCellHighlightType highlightType = ReportCellHighlightType.fromErrorsAndWarnings(validationErrors, validationWarnings);
        Map<ValidationReportColumnKey, ValidationResult> columnResults = getReportColumnsWithValidationResult(
            imageRules,
            consistencyRules,
            deviationRules,
            imageValidationResult
        );
        int errorWarningCount = validationErrors.size() + validationWarnings.size();

        return Stream.of(
            ReportRow.ofImage(
                Stream.concat(
                    Stream.concat(
                        Stream.of(
                            ReportCell.of(filepath.toAbsolutePath().normalize().toString()), // path
                            ReportCell.of(Objects.toString(filepath.getParent().getFileName(), "")), // directory name
                            ReportCell.of(Objects.toString(filepath.getFileName(), ""), highlightType), // file name
                            ReportCell.empty(highlightType), // directory passed validation
                            ReportCell.of(passedValidation, highlightType), // file passed validation
                            ReportCell.of(validationErrors, validationWarnings), // validation error or warning
                            ReportCell.of(errorWarningCount > 0 ? errorWarningCount : null, highlightType)
                        ),
                        propertyColumnKeys
                            .stream()
                            .map(propertyColumnKey ->
                                ReportCell.of(
                                    extractPropertyOrNull(propertyColumnKey.property(), imageValidationResult.getExtractionResult()),
                                    ReportCellHighlightType.fromValidationResult(
                                        columnResults.getOrDefault(propertyColumnKey, ValidationResult.fromSuccess())
                                    ),
                                    getNoteFromValidationResult(
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

    private static List<ValidationReportColumnKey> findAllReportColumns(
        List<ImageRule> imageRules,
        List<DirectoryConsistencyCondition> consistencyRules,
        List<DirectoryPropertyDeviationsRule> deviationRules,
        List<ValidationReportColumn> extraColumns
    ) {
        Set<ValidationReportColumnKey> keySet = new LinkedHashSet<>();

        // image rules
        for (ImageRule rule : imageRules) {
            for (ValidationReportColumn column : rule.getReportColumns()) {
                keySet.add(ValidationReportColumnKey.of(column));
            }
        }

        // consistency rules
        for (DirectoryConsistencyCondition consistencyRule : consistencyRules) {
            if (consistencyRule.isReportColumn()) {
                keySet.add(new ValidationReportColumnKey(consistencyRule.getLabel(), consistencyRule.getProperty()));
            }
        }

        // deviation rules
        for (DirectoryPropertyDeviationsRule deviationRule : deviationRules) {
            if (deviationRule.isReportColumn()) {
                keySet.add(new ValidationReportColumnKey(deviationRule.getLabel(), deviationRule.getProperty()));
            }
        }

        // extra columns
        for (ValidationReportColumn column : extraColumns) {
            keySet.add(ValidationReportColumnKey.of(column));
        }

        return new ArrayList<>(keySet);
    }

    private static Map<ValidationReportColumnKey, ValidationResult> getReportColumnsWithValidationResult(
        List<ImageRule> imageRules,
        List<DirectoryConsistencyCondition> consistencyRules,
        List<DirectoryPropertyDeviationsRule> deviationRules,
        ImageValidationResult imageValidationResult
    ) {
        return mergeReportColumnsWithValidationResults(
            mergeReportColumnsWithValidationResults(
                getImageRuleReportColumnsWithValidationResult(imageRules, imageValidationResult),
                getConsistentPropertyReportColumnsWithValidationResult(consistencyRules, imageValidationResult)
            ),
            getPropertyDeviationReportColumnsWithValidationResult(deviationRules, imageValidationResult)
        );
    }

    private static Map<ValidationReportColumnKey, ValidationResult> getPropertyDeviationReportColumnsWithValidationResult(
        List<DirectoryPropertyDeviationsRule> deviationRules,
        ImageValidationResult imageValidationResult
    ) {
        List<ValidationResult> ruleResults = imageValidationResult.getPropertyDeviationEvaluationResults();
        if (deviationRules.size() != ruleResults.size()) {
            throw new IllegalArgumentException("number of property deviation rules does not match number of validation results");
        }

        Map<ValidationReportColumnKey, ValidationResult> results = new LinkedHashMap<>();

        int i = 0;
        for (DirectoryPropertyDeviationsRule deviationRule : deviationRules) {
            ValidationReportColumnKey columnKey = new ValidationReportColumnKey(deviationRule.getLabel(), deviationRule.getProperty());
            results.put(columnKey, ruleResults.get(i));
            i += 1;
        }

        return results;
    }

    private static Map<ValidationReportColumnKey, ValidationResult> getConsistentPropertyReportColumnsWithValidationResult(
        List<DirectoryConsistencyCondition> consistencyRules,
        ImageValidationResult imageValidationResult
    ) {
        List<ValidationResult> ruleResults = imageValidationResult.getConsistentPropertyEvaluationResults();
        if (consistencyRules.size() != ruleResults.size()) {
            throw new IllegalArgumentException("number of consistency rules does not match number of validation results");
        }

        Map<ValidationReportColumnKey, ValidationResult> results = new LinkedHashMap<>();

        int i = 0;
        for (DirectoryConsistencyCondition consistencyRule : consistencyRules) {
            ValidationReportColumnKey columnKey = new ValidationReportColumnKey(consistencyRule.getLabel(), consistencyRule.getProperty());
            results.put(columnKey, ruleResults.get(i));
            i += 1;
        }

        return results;
    }

    private static Map<ValidationReportColumnKey, ValidationResult> getImageRuleReportColumnsWithValidationResult(
        List<ImageRule> imageRules,
        ImageValidationResult imageValidationResult
    ) {
        List<ValidationResult> ruleResults = imageValidationResult.getImageRuleEvaluationResults();
        if (imageRules.size() != ruleResults.size()) {
            throw new IllegalArgumentException("number of image rules does not match number of validation results");
        }

        Map<ValidationReportColumnKey, ValidationResult> results = new LinkedHashMap<>();

        int i = 0;
        for (ImageRule rule : imageRules) {
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

    private static Map<ValidationReportColumnKey, ValidationResult> mergeReportColumnsWithValidationResults(
        Map<ValidationReportColumnKey, ValidationResult> resultsA,
        Map<ValidationReportColumnKey, ValidationResult> resultsB
    ) {
        Map<ValidationReportColumnKey, ValidationResult> results = new LinkedHashMap<>(resultsA);

        for (Map.Entry<ValidationReportColumnKey, ValidationResult> entry : resultsB.entrySet()) {
            if (!results.containsKey(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            } else {
                results.put(entry.getKey(), ValidationResult.of(List.of(entry.getValue(), results.get(entry.getKey()))));
            }
        }

        return results;
    }

    private static String getNoteFromValidationResult(ValidationResult validationResult) {
        return List.of(validationResult.getErrors(), validationResult.getWarnings())
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.joining("\n"));
    }

    private static Stream<ReportRow> writeMetadata(List<String> commandArgs, Path configFilepath, Map<String, String> extractorVersions) {
        return Stream.of(
            Stream.of(ReportRow.empty()),
            writeCommandMetadata(commandArgs, configFilepath),
            Stream.of(ReportRow.empty()),
            writeVersionMetadata(extractorVersions)
        ).flatMap(Function.identity());
    }

    private static Stream<ReportRow> writeCommandMetadata(List<String> commandArgs, Path configFilepath) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        return Stream.of(
            ReportRow.ofMetadata(
                Stream.of(ReportCell.empty(), ReportCell.of("generated on"), ReportCell.of(ZonedDateTime.now().format(formatter))).toList()
            ),
            ReportRow.ofMetadata(
                Stream.of(
                    ReportCell.empty(),
                    ReportCell.of("with arguments"),
                    ReportCell.of(commandArgs.stream().collect(Collectors.joining(" ")))
                ).toList()
            ),
            ReportRow.ofMetadata(
                Stream.of(
                    ReportCell.empty(),
                    ReportCell.of("using configuration file"),
                    ReportCell.of(Objects.nonNull(configFilepath) ? configFilepath.toAbsolutePath().normalize().toString() : "")
                ).toList()
            )
        );
    }

    private static Stream<ReportRow> writeVersionMetadata(Map<String, String> extractorVersions) {
        return Stream.of(
            Stream.of(
                ReportRow.ofMetadata(
                    Stream.of(
                        ReportCell.empty(),
                        ReportCell.of("operating system"),
                        ReportCell.of(
                            String.format(
                                "%s %s %s",
                                System.getProperty("os.name"),
                                System.getProperty("os.version"),
                                System.getProperty("os.arch")
                            )
                        )
                    ).toList()
                ),
                ReportRow.ofMetadata(
                    Stream.of(
                        ReportCell.empty(),
                        ReportCell.of("java"),
                        ReportCell.of(
                            String.format(
                                "%s (%s %s %s)",
                                System.getProperty("java.version"),
                                System.getProperty("java.vendor"),
                                System.getProperty("java.vm.name"),
                                System.getProperty("java.vm.version")
                            )
                        )
                    ).toList()
                ),
                ReportRow.ofMetadata(Stream.of(ReportCell.empty(), ReportCell.of("stabiv"), ReportCell.of(Constants.VERSION)).toList())
            ),
            extractorVersions
                .entrySet()
                .stream()
                .map(e ->
                    ReportRow.ofMetadata(Stream.of(ReportCell.empty(), ReportCell.of(e.getKey()), ReportCell.of(e.getValue())).toList())
                )
        ).flatMap(Function.identity());
    }
}
