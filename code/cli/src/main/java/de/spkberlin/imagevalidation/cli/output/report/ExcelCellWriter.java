package de.spkberlin.imagevalidation.cli.output.report;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationExcelSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationExcelStyleElement;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelCellWriter {

    private XSSFWorkbook workbook;
    private Sheet mainSheet;
    private Sheet listSheet;
    private CreationHelper creationHelper;
    private DataValidationHelper validationHelper;
    private Drawing<?> drawingPatriarch;
    private AtomicInteger listIndex;
    private Boolean onlyFirstListEntry;

    private XSSFCellStyle defaultStyle;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle warningStyle;
    private XSSFCellStyle errorStyle;

    /**
     * Initialize this cell writer for a workbook and sheet.
     *
     * @param workbook the workbook
     * @param mainSheet the sheet
     */
    public ExcelCellWriter(XSSFWorkbook workbook, Sheet mainSheet, ConfigurationExcelSection config) {
        this.workbook = workbook;
        this.mainSheet = mainSheet;

        this.onlyFirstListEntry = config.getLayout().isOnlyFirstListEntry();
        this.listSheet = workbook.createSheet("Lists");
        this.workbook.setSheetHidden(workbook.getSheetIndex(listSheet), true);
        this.listIndex = new AtomicInteger(0);
        this.validationHelper = mainSheet.getDataValidationHelper();
        this.creationHelper = workbook.getCreationHelper();
        this.drawingPatriarch = mainSheet.createDrawingPatriarch();

        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        this.defaultStyle = createCellStyle(workbook, boldFont, config.getStyle().getDefaultElement());
        this.headerStyle = createCellStyle(workbook, boldFont, config.getStyle().getHeader());
        this.warningStyle = createCellStyle(workbook, boldFont, config.getStyle().getWarning());
        this.errorStyle = createCellStyle(workbook, boldFont, config.getStyle().getError());
    }

    /**
     * Write the value for a single cell of the report.
     *
     * @param cell the sheet cell that is written
     * @param reportCell the report cell containing validation information
     */
    public void writeCell(Cell cell, ReportCell reportCell) {
        // write value
        if (ReportCellValueType.text.equals(reportCell.getValueType())) {
            cell.setCellValue(reportCell.getText());
        } else if (ReportCellValueType.integer.equals(reportCell.getValueType())) {
            cell.setCellValue(reportCell.getInteger());
        } else if (ReportCellValueType.list.equals(reportCell.getValueType())) {
            if (!onlyFirstListEntry) {
                addListAsConstraint(workbook, mainSheet, listSheet, validationHelper, cell, reportCell.getList(), listIndex);
            }
            cell.setCellValue(reportCell.getList().stream().findFirst().orElse(""));
        }

        if (Objects.nonNull(reportCell.getNote()) && !reportCell.getNote().isBlank()) {
            addNote(cell, reportCell.getNote());
        }

        // apply color
        if (ReportCellHighlightType.error.equals(reportCell.getHighlightType())) {
            cell.setCellStyle(errorStyle);
        } else if (ReportCellHighlightType.warning.equals(reportCell.getHighlightType())) {
            cell.setCellStyle(warningStyle);
        } else {
            if (cell.getRowIndex() == 0) {
                cell.setCellStyle(headerStyle);
            } else {
                cell.setCellStyle(defaultStyle);
            }
        }
    }

    private void addNote(Cell cell, String note) {
        ClientAnchor anchor = this.creationHelper.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 5);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + note.split("\n").length);

        Comment comment = this.drawingPatriarch.createCellComment(anchor);
        comment.setString(this.creationHelper.createRichTextString(note));
        comment.setAuthor("System");

        cell.setCellComment(comment);
    }

    /**
     * Add an interactive drop-down list to a cell to visualize all list entries.
     *
     * <p>List values are stored row-wise in a separate sheet. The drop-down validation constraint references the list via a formula.
     * In order to preserve space, empty lists will not create a row.
     *
     * @param workbook the workbook
     * @param mainSheet the main sheet that contains the report
     * @param listSheet the extra sheet that is used to store list values
     * @param validationHelper a help class required to add validation constraints (drop-downs)
     * @param cell the cell of the main sheet that is supposed to render the list
     * @param list the list of values
     * @param listIndex a global index that is incremented if a new list row is created
     */
    private static void addListAsConstraint(
        Workbook workbook,
        Sheet mainSheet,
        Sheet listSheet,
        DataValidationHelper validationHelper,
        Cell cell,
        List<String> list,
        AtomicInteger listIndex
    ) {
        if (list.size() > 1) {
            Row row = listSheet.createRow(listIndex.get());
            for (int i = 0; i < list.size(); i++) {
                row.createCell(i).setCellValue(list.get(i));
            }
            String columnStartName = CellReference.convertNumToColString(0);
            String columnEndName = CellReference.convertNumToColString(list.size() - 1);
            String formula = String.format("Lists!$%s$%d:$%s$%d", columnStartName, listIndex.get() + 1, columnEndName, listIndex.get() + 1);
            DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(formula);
            mainSheet.addValidationData(
                validationHelper.createValidation(
                    constraint,
                    new CellRangeAddressList(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex())
                )
            );
            listIndex.incrementAndGet();
        }
    }

    private static XSSFCellStyle createCellStyle(XSSFWorkbook workbook, Font boldFont, ConfigurationExcelStyleElement style) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();

        if (style.isBold()) {
            cellStyle.setFont(boldFont);
        }

        if (Objects.nonNull(style.getBackgroundColor())) {
            cellStyle.setFillForegroundColor(new XSSFColor(java.awt.Color.decode(style.getBackgroundColor()), null));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        if (Objects.nonNull(style.getBorderColor())) {
            XSSFColor borderColor = new XSSFColor(java.awt.Color.decode(style.getBorderColor()), null);

            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setTopBorderColor(borderColor);
            cellStyle.setBottomBorderColor(borderColor);
            cellStyle.setLeftBorderColor(borderColor);
            cellStyle.setRightBorderColor(borderColor);
        }
        return cellStyle;
    }
}
