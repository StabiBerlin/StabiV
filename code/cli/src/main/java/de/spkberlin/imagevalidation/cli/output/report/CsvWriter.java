package de.spkberlin.imagevalidation.cli.output.report;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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
    public static void writeReport(Path outputPath, List<List<ReportCell>> report) {
        logger.info("write csv output file '{}'", outputPath);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), CSVFormat.EXCEL)) {
            for (List<ReportCell> reportRow : report) {
                printer.printRecord(reportRow.stream().map(ReportCell::getText));
            }
        } catch (IOException e) {
            logger.error("exception while writing CSV report", e);
        }
    }
}
