package de.spkberlin.imagevalidation.cli.output.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilters;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
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
    public static void writeReport(Path outputPath, List<ReportRow> report) throws IOException {
        logger.info("write excel output file '{}'", outputPath);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet mainSheet = workbook.createSheet("Report");
        ExcelCellWriter cellWriter = new ExcelCellWriter(workbook, mainSheet);

        int maxColumnId = 0;
        int rowId = 0;

        for (ReportRow reportRow : report) {
            Row row = mainSheet.createRow(rowId);
            int columnId = 0;

            for (ReportCell reportCell : reportRow.getCells()) {
                Cell cell = row.createCell(columnId);
                cellWriter.writeCell(cell, reportCell);
                maxColumnId = Math.max(columnId, maxColumnId);
                columnId += 1;
            }

            // fill with empty cells
            while (columnId <= maxColumnId) {
                Cell cell = row.createCell(columnId);
                cellWriter.writeCell(cell, ReportCell.empty());
                columnId += 1;
            }

            rowId += 1;
        }

        groupImageRows(mainSheet, report);
        autoFitColumns(mainSheet, maxColumnId + 1, 512);
        addTableFilters(mainSheet, rowId + 1, maxColumnId + 1);
        hideValidImagesByDefault(mainSheet, report);

        mainSheet.createFreezePane(6, 2);
        mainSheet.setColumnHidden(0, true);

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
     * Applies the table column filter "false or empty" to the "Image Passed Validation" column and hides all corresponding rows.
     *
     * @param sheet the main sheet
     */
    private static void hideValidImagesByDefault(XSSFSheet sheet, List<ReportRow> report) {
        // apply table column filter
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        CTAutoFilter autoFilter = ctWorksheet.getAutoFilter();

        CTFilterColumn filterColumn = autoFilter.addNewFilterColumn();
        filterColumn.setColId(4);

        CTFilters filters = filterColumn.addNewFilters();
        filters.setBlank(true);
        filters.addNewFilter().setVal("false");

        // actually hide rows
        int rowId = 0;
        for (ReportRow reportRow : report) {
            if (ReportRowType.image.equals(reportRow.getType()) && reportRow.isValid()) {
                sheet.getRow(rowId).setZeroHeight(true);
            }
            rowId += 1;
        }
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

    /**
     * Add grouping such that all images for a directory can be collapsed or expanded.
     *
     * @param report the report
     * @param sheet the sheet
     */
    private static void groupImageRows(Sheet sheet, List<ReportRow> report) {
        int rowId = 0;
        int startImageRowId = 0;
        Boolean lastDirectoryValid = true;
        ReportRowType lastRowType = ReportRowType.header;

        for (ReportRow reportRow : report) {
            if (ReportRowType.directory.equals(reportRow.getType())) {
                lastDirectoryValid = reportRow.isValid();
            }
            if (!ReportRowType.image.equals(lastRowType) && ReportRowType.image.equals(reportRow.getType())) {
                startImageRowId = rowId;
            }
            if (ReportRowType.image.equals(lastRowType) && !ReportRowType.image.equals(reportRow.getType())) {
                sheet.groupRow(startImageRowId, rowId - 1);
                sheet.setRowGroupCollapsed(startImageRowId, lastDirectoryValid);
            }
            lastRowType = reportRow.getType();
            rowId += 1;
        }
    }
}
