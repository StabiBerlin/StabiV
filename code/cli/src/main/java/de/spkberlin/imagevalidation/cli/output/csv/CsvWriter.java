package de.spkberlin.imagevalidation.cli.output.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvWriter {

    public static void writeReport(Map<Path, List<Path>> filesByDirectory, Path path) {
        writeReport(filesByDirectory, path, CSVFormat.EXCEL);
    }

    public static void writeReport(Map<Path, List<Path>> filesByDirectory, Path path, CSVFormat format) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(path.toFile()), format)) {
            printer.printRecord("Ordner", "Datei", "Prüfsumme", "Prüfung bestanden", "TIFF 6.0 Baseline wellformed");

            for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
                Path directory = entry.getKey();
                printer.printRecord(directory.toString());

                for (Path filename : entry.getValue()) {
                    printer.printRecord(null, filename.toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
