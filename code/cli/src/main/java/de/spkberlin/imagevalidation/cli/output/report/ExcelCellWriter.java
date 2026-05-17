package de.spkberlin.imagevalidation.cli.output.report;

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
import org.apache.poi.ss.usermodel.IndexedColors;
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
    public ExcelCellWriter(XSSFWorkbook workbook, Sheet mainSheet) {
        this.workbook = workbook;
        this.mainSheet = mainSheet;

        this.listSheet = workbook.createSheet("Lists");
        this.workbook.setSheetHidden(workbook.getSheetIndex(listSheet), true);
        this.listIndex = new AtomicInteger(0);
        this.validationHelper = mainSheet.getDataValidationHelper();
        this.creationHelper = workbook.getCreationHelper();
        this.drawingPatriarch = mainSheet.createDrawingPatriarch();

        this.defaultStyle = createBaseCellStyle(workbook);

        this.headerStyle = createBaseCellStyle(workbook);
        this.headerStyle.setFillForegroundColor(new XSSFColor(java.awt.Color.decode("#DDDDDD"), null));
        this.headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        this.headerStyle.setFont(boldFont);

        this.warningStyle = createBaseCellStyle(workbook);
        this.warningStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        this.warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.errorStyle = createBaseCellStyle(workbook);
        this.errorStyle.setFillForegroundColor(new XSSFColor(java.awt.Color.decode("#FF7D55"), null));
        this.errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.errorStyle.setTopBorderColor(new XSSFColor(java.awt.Color.decode("#8c3b22"), null));
        this.errorStyle.setBottomBorderColor(new XSSFColor(java.awt.Color.decode("#8c3b22"), null));
        this.errorStyle.setLeftBorderColor(new XSSFColor(java.awt.Color.decode("#8c3b22"), null));
        this.errorStyle.setRightBorderColor(new XSSFColor(java.awt.Color.decode("#8c3b22"), null));
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
            addListAsConstraint(workbook, mainSheet, listSheet, validationHelper, cell, reportCell.getList(), listIndex);
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
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + note.split("\n").length);

        Comment comment = this.drawingPatriarch.createCellComment(anchor);
        comment.setString(this.creationHelper.createRichTextString(note));
        comment.setAuthor("System");

        cell.setCellComment(comment);
    }

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
            for (int i = 0; i < list.size(); i++) {
                Row row = Objects.isNull(listSheet.getRow(i)) ? listSheet.createRow(i) : listSheet.getRow(i);
                row.createCell(listIndex.get()).setCellValue(list.get(i));
            }
            String columnName = CellReference.convertNumToColString(listIndex.get());
            String formula = String.format("Lists!$%s$1:$%s$%d", columnName, columnName, list.size());
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

    private static XSSFCellStyle createBaseCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return style;
    }
}
