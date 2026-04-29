package de.spkberlin.imagevalidation.cli.output.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelWriter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelWriter.class);

    /**
     * Write the report as Excel file.
     *
     * @param outputPath the path to the file that is generated
     * @throws IOException in case writing fails
     */
    public static void writeReport(Path outputPath, List<List<ReportCell>> report) throws IOException {
        logger.info("write excel output file '{}'", outputPath);
        Workbook workbook = new XSSFWorkbook();
        CellStyle yellowStyle = workbook.createCellStyle();
        yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Sheet sheet = workbook.createSheet("Report");
        int maxColumnId = 0;

        int rowId = 0;
        for (List<ReportCell> reportRow : report) {
            Row row = sheet.createRow(rowId);
            int columnId = 0;

            for (ReportCell reportCell : reportRow) {
                Cell cell = row.createCell(columnId);
                cell.setCellValue(reportCell.getText());
                if (reportCell.isHighlighted()) {
                    cell.setCellStyle(yellowStyle);
                }

                maxColumnId = Math.max(columnId, maxColumnId);
                columnId += 1;
            }

            rowId += 1;
        }

        autoFitColumns(sheet, maxColumnId + 1, 512);
        addTableFilters(sheet, rowId + 1, maxColumnId + 1);

        try (FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
            workbook.write(out);
        }

        workbook.close();
    }

    /**
     * Makes the sheet filterable for all columns.
     *
     * @param sheet the sheet
     * @param rows the number of rows
     * @param columns the number of columns
     */
    private static void addTableFilters(Sheet sheet, int rows, int columns) {
        sheet.setAutoFilter(new CellRangeAddress(0, rows - 1, 0, columns - 1));
    }

    /**
     * Increase column size to fit data.
     *
     * @param sheet the sheet
     * @param columns the number of columns
     */
    private static void autoFitColumns(Sheet sheet, int columns, int padding) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + padding);
        }
    }
}
