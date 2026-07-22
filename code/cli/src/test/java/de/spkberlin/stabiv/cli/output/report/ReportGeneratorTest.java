package de.spkberlin.stabiv.cli.output.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.spkberlin.stabiv.cli.validation.ValidateAllResult;
import de.spkberlin.stabiv.common.extract.ExtractionResult;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryValidationResult;
import de.spkberlin.stabiv.common.validation.images.state.ImageValidationResult;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class ReportGeneratorTest {

    /**
     * Test that a simple report can be generated from validation results of a single file.
     */
    @Test
    public void testGeneratingSingleFileReport() {
        generateAndCheckEmptyReportForSingleFile(Path.of("/directory"), Path.of("/directory/file.tif"));
    }

    /**
     * Test that a simple report can be generated from validation results of a single file.
     */
    @Test
    @EnabledOnOs({ OS.LINUX })
    public void testGeneratingReportForFileInLinuxRootDirectory() {
        generateAndCheckEmptyReportForSingleFile(Path.of("/"), Path.of("/file.tif"));
    }

    /**
     * Test that a simple report can be generated from validation results of a single file.
     */
    @Test
    @EnabledOnOs({ OS.WINDOWS })
    public void testGeneratingReportForFileInWindowsRootDirectory() {
        generateAndCheckEmptyReportForSingleFile(Path.of("C:\\"), Path.of("C:\\file.tif"));
    }

    private void generateAndCheckEmptyReportForSingleFile(Path diretoryPath, Path filePath) {
        ValidateAllResult validateAllResult = createEmptyValidateAllResult(diretoryPath, filePath);

        List<ReportRow> reportRows = ReportGenerator.generateReport(
            validateAllResult,
            List.of(), // image rules
            List.of(), // consistency rules
            List.of(), // deviation rules
            List.of(), // property columns
            List.of("command", "--args"),
            Path.of("/directory/config.yaml"),
            Map.of("exiftool", "version")
        );

        assertEquals(15, reportRows.size());
    }

    private ValidateAllResult createEmptyValidateAllResult(Path diretoryPath, Path filePath) {
        Map<Path, List<Path>> filesByDirectory = Map.of(diretoryPath, List.of(filePath));

        ExtractionResult extractionResult = new ExtractionResult(Map.of("property", "value"));
        List<ValidationResult> imageRuleValidationResults = List.of();
        List<ValidationResult> propertyDeviationResults = List.of();
        List<ValidationResult> consistentPropertyEvaluationResults = List.of();
        ValidationResult consecutiveNumberingEvaluationResult = ValidationResult.fromSuccess();

        ImageValidationResult imageValidationResult = new ImageValidationResult(
            filePath,
            extractionResult,
            imageRuleValidationResults,
            propertyDeviationResults,
            consistentPropertyEvaluationResults,
            consecutiveNumberingEvaluationResult,
            "checksum"
        );

        Map<Path, ImageValidationResult> imageValidationResults = Map.of(filePath, imageValidationResult);

        DirectoryValidationResult directoryValidationResult = new DirectoryValidationResult(
            diretoryPath,
            imageValidationResults,
            ValidationResult.fromSuccess(),
            1
        );

        Map<Path, DirectoryValidationResult> directoryValidationResults = Map.of(diretoryPath, directoryValidationResult);

        return new ValidateAllResult(filesByDirectory, imageValidationResults, directoryValidationResults);
    }
}
