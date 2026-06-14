package de.spkberlin.stabiv.cli.output.report;

import de.spkberlin.stabiv.cli.configuration.state.ConfigurationCsvSection;
import de.spkberlin.stabiv.cli.path.FileUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvWriter {

    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    /**
     * Write the CSV validation report file.
     *
     * @param config the csv report configuration section
     * @param report the report to write
     * @param inputBaseDirectory the base directory of the input config section
     */
    public static void writeReport(ConfigurationCsvSection config, List<ReportRow> report, Path inputBaseDirectory) {
        Path outputPath = resolveOutputPath(config, inputBaseDirectory);
        logger.info("write csv output file '{}'", outputPath);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), CSVFormat.EXCEL)) {
            for (ReportRow reportRow : report) {
                printer.printRecord(
                    reportRow
                        .getCells()
                        .stream()
                        .map(cell -> {
                            if (ReportCellValueType.text.equals(cell.getValueType())) {
                                return Objects.nonNull(cell.getText()) ? cell.getText() : "";
                            } else if (ReportCellValueType.integer.equals(cell.getValueType())) {
                                return Objects.nonNull(cell.getInteger()) ? cell.getInteger().toString() : "";
                            } else if (ReportCellValueType.list.equals(cell.getValueType())) {
                                return Objects.nonNull(cell.getList()) ? cell.getList().stream().collect(Collectors.joining("; ")) : "";
                            } else if (ReportCellValueType.empty.equals(cell.getValueType())) {
                                return "";
                            }
                            return "";
                        })
                );
            }
        } catch (IOException e) {
            logger.error("exception while writing CSV report", e);
        }
    }

    /**
     * Resolve output path for the csv report by applying variable substitions.
     *
     * @param config the csv report configuration
     * @param inputBaseDirectory the base directory defined in the input config section
     * @return the final output path
     */
    private static Path resolveOutputPath(ConfigurationCsvSection config, Path inputBaseDirectory) {
        return FileUtils.modifyPathIfExists(
            config
                .getPath()
                .getBaseDirectory()
                .resolve(ReportFile.resolveFilepathTemplate(config.getPath().getPath(), inputBaseDirectory))
                .normalize()
        );
    }
}
