package de.spkberlin.stabiv.cli.output.report;

import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExcelSection;
import de.spkberlin.stabiv.cli.path.FileUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelWriter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelWriter.class);

    /**
     * Write the report as Excel file.
     *
     * @param config the excel report configuration
     * @param report the generated report
     * @param inputBaseDirectory the base directory of the input config section
     * @throws IOException in case writing fails
     */
    public static void writeReport(ConfigurationExcelSection config, List<ReportRow> report, Path inputBaseDirectory) throws IOException {
        Path outputPath = resolveOutputPath(config, inputBaseDirectory);

        logger.info("write excel output file '{}'", outputPath);
        Workbook workbook = createWorkbook(config, report);
        try (FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
            workbook.write(out);
        }
        workbook.close();
    }

    /**
     * Create a excel workbook from the generated report.
     *
     * @param config the excel report configuration
     * @param report the generated report
     * @return the excel workbook
     */
    private static Workbook createWorkbook(ConfigurationExcelSection config, List<ReportRow> report) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet mainSheet = workbook.createSheet("Report");
        ExcelCellWriter cellWriter = new ExcelCellWriter(workbook, mainSheet, config);

        int maxColumnId = 0;
        int rowId = 0;
        boolean fitted = false;

        for (ReportRow reportRow : report) {
            Row row = mainSheet.createRow(rowId);
            int columnId = 0;

            // auto fit columns as soon as all validation results are written (ignoring metadata rows)
            if (!fitted && ReportRowType.metadata.equals(reportRow.getType())) {
                autoFitColumns(mainSheet, maxColumnId + 1, config);
                fitted = true;
            }

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

        groupImageRows(mainSheet, report, config);
        addTableFilters(mainSheet, rowId + 1, maxColumnId + 1);
        createFreezePane(mainSheet, config);
        hideColumns(mainSheet, config);

        return workbook;
    }

    private static void hideColumns(XSSFSheet mainSheet, ConfigurationExcelSection config) {
        if (Objects.nonNull(config.getLayout().getHiddenColumns())) {
            for (Integer columnId : config.getLayout().getHiddenColumns()) {
                mainSheet.setColumnHidden(columnId, true);
            }
        }
    }

    private static void createFreezePane(XSSFSheet mainSheet, ConfigurationExcelSection config) {
        if (config.getLayout().getFixed().getRows() > 0 || config.getLayout().getFixed().getColumns() > 0) {
            mainSheet.createFreezePane(config.getLayout().getFixed().getColumns(), config.getLayout().getFixed().getRows());
        }
    }

    /**
     * Resolve output path for the excel report by applying variable substitions.
     *
     * @param config the excel report configuration
     * @param inputBaseDirectory the base directory defined in the input config section
     * @return the final output path
     */
    private static Path resolveOutputPath(ConfigurationExcelSection config, Path inputBaseDirectory) {
        return FileUtils.modifyPathIfExists(
            config
                .getPath()
                .getBaseDirectory()
                .resolve(ReportFile.resolveFilepathTemplate(config.getPath().getPath(), inputBaseDirectory))
                .normalize()
        );
    }

    /**
     * Makes the sheet filterable for all columns.
     *
     * @param sheet the sheet
     * @param rows the number of rows
     * @param columns the number of columns
     */
    private static void addTableFilters(XSSFSheet sheet, int rows, int columns) {
        AreaReference area = new AreaReference(
            new CellReference(0, 0),
            new CellReference(rows - 1, columns - 1),
            SpreadsheetVersion.EXCEL2007
        );

        XSSFTable table = sheet.createTable(area);
        CTTable ctTable = table.getCTTable();
        ctTable.setName("Table1");
        ctTable.setDisplayName("Table1");
        // ctTable.setRef(area.formatAsString());

        CTTableStyleInfo style = Objects.nonNull(ctTable.getTableStyleInfo())
            ? ctTable.getTableStyleInfo()
            : ctTable.addNewTableStyleInfo();
        style.setName("TableStyleMedium1");
        style.setShowRowStripes(true);
        style.setShowColumnStripes(false);

        ctTable.addNewAutoFilter().setRef(area.formatAsString());
    }

    /**
     * Increase column size to fit data.
     *
     * @param sheet the sheet
     * @param columns the number of columns
     */
    private static void autoFitColumns(Sheet sheet, int columns, ConfigurationExcelSection config) {
        if (!config.getLayout().getAutoFitColumns().isEnabled()) {
            return;
        }

        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + config.getLayout().getAutoFitColumns().getPadding());
        }
    }

    /**
     * Add grouping such that all images for a directory can be collapsed or expanded.
     *
     * @param report the report
     * @param sheet the sheet
     */
    private static void groupImageRows(Sheet sheet, List<ReportRow> report, ConfigurationExcelSection config) {
        if (!config.getLayout().isGroupDirectories()) {
            return;
        }

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
                sheet.setRowGroupCollapsed(startImageRowId, config.getLayout().isCollapseValidDirectories() ? lastDirectoryValid : false);
            }
            lastRowType = reportRow.getType();
            rowId += 1;
        }
    }
}
