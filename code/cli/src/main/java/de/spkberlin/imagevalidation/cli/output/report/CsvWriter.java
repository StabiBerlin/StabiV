package de.spkberlin.imagevalidation.cli.output.report;

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
     * @param outputPath output filepath of the CSV file
     * @param report the report to write
     */
    public static void writeReport(Path outputPath, List<ReportRow> report) {
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
}
