package data.export;

import data.service.MonthlyReportData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelExporter {
    private static final int COL_DATE = 0;
    private static final int COL_DESCRIPTION = 1;
    private static final int COL_CURRENCY = 2;
    private static final int COL_AMOUNT = 3;
    private static final int COL_AMOUNT_KZT = 4;
    private static final int TOTAL_COLUMNS = 5;

    public void export(MonthlyReportData report, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            Map<String, CellStyle> styles = createStyles(workbook);

            int currentRow = 0;
            currentRow = writeHeader(sheet, report, styles, currentRow);
            currentRow = writeCategories(sheet, report, styles, currentRow);
            writeGrandTotal(sheet, report, styles, currentRow);

            autoSizeColumns(sheet);
            saveFile(workbook, filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }

    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        DataFormat format = workbook.createDataFormat();
        String numberFormat = "#,##0.00";

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        styles.put("title", titleStyle);

        Font categoryFont = workbook.createFont();
        categoryFont.setBold(true);
        categoryFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle categoryStyle = workbook.createCellStyle();
        categoryStyle.setFont(categoryFont);
        categoryStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        categoryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("category", categoryStyle);

        Font colHeaderFont = workbook.createFont();
        colHeaderFont.setBold(true);
        CellStyle colHeaderStyle = workbook.createCellStyle();
        colHeaderStyle.setFont(colHeaderFont);
        colHeaderStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        colHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        colHeaderStyle.setBorderBottom(BorderStyle.THIN);
        styles.put("colHeader", colHeaderStyle);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        styles.put("data", dataStyle);

        CellStyle amountStyle = workbook.createCellStyle();
        amountStyle.setDataFormat(format.getFormat(numberFormat));
        amountStyle.setBorderBottom(BorderStyle.THIN);
        amountStyle.setBorderLeft(BorderStyle.THIN);
        amountStyle.setBorderRight(BorderStyle.THIN);
        amountStyle.setBorderTop(BorderStyle.THIN);
        amountStyle.setAlignment(HorizontalAlignment.RIGHT);
        styles.put("amount", amountStyle);

        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.setFont(totalFont);
        totalStyle.setDataFormat(format.getFormat(numberFormat));
        totalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        totalStyle.setBorderTop(BorderStyle.DOUBLE);
        totalStyle.setAlignment(HorizontalAlignment.RIGHT);
        styles.put("total", totalStyle);

        CellStyle totalLabelStyle = workbook.createCellStyle();
        totalLabelStyle.setFont(totalFont);
        totalLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        totalLabelStyle.setBorderTop(BorderStyle.DOUBLE);
        styles.put("totalLabel", totalLabelStyle);

        Font grandTotalFont = workbook.createFont();
        grandTotalFont.setBold(true);
        grandTotalFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle grandTotalStyle = workbook.createCellStyle();
        grandTotalStyle.setFont(grandTotalFont);
        grandTotalStyle.setDataFormat(format.getFormat(numberFormat));
        grandTotalStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        grandTotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        grandTotalStyle.setAlignment(HorizontalAlignment.RIGHT);
        styles.put("grandTotal", grandTotalStyle);

        CellStyle grandTotalLabelStyle = workbook.createCellStyle();
        grandTotalLabelStyle.setFont(grandTotalFont);
        grandTotalLabelStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        grandTotalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("grandTotalLabel", grandTotalLabelStyle);

        return styles;
    }

    private int writeHeader(Sheet sheet, MonthlyReportData report, Map<String, CellStyle> styles, int currentRow) {
        Row titleRow = sheet.createRow(currentRow);
        titleRow.setHeightInPoints(24);

        for (int i = 0; i < TOTAL_COLUMNS; i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellStyle(styles.get("title"));
        }

        Cell titleCell = titleRow.getCell(0);
        titleCell.setCellValue("Monthly Report: " + report.month() + " " + report.year());

        sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, TOTAL_COLUMNS - 1));

        return currentRow + 2;
    }

    private int writeCategories(Sheet sheet, MonthlyReportData report, Map<String, CellStyle> styles, int currentRow) {
        for (Map.Entry<String, List<TransactionReportLine>> entry : report.transactionsByCategory().entrySet()) {
            currentRow = writeCategory(sheet, styles, currentRow, entry.getKey(), entry.getValue(), report);
            currentRow++;
        }
        return currentRow;
    }

    private int writeCategory(Sheet sheet, Map<String, CellStyle> styles, int currentRow,
                              String categoryName, List<TransactionReportLine> lines,
                              MonthlyReportData report) {
        Row categoryRow = sheet.createRow(currentRow);
        for (int i = 0; i < TOTAL_COLUMNS; i++) {
            categoryRow.createCell(i).setCellStyle(styles.get("category"));
        }
        categoryRow.getCell(0).setCellValue("Category: " + categoryName);
        sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, TOTAL_COLUMNS - 1));
        currentRow++;

        currentRow = writeColumnHeaders(sheet, styles, currentRow);

        for (TransactionReportLine line : lines) {
            currentRow = writeTransactionRow(sheet, styles, currentRow, line);
        }

        Row totalRow = sheet.createRow(currentRow);
        for (int i = 0; i < TOTAL_COLUMNS; i++) {
            totalRow.createCell(i).setCellStyle(styles.get("totalLabel"));
        }
        totalRow.getCell(0).setCellValue("Category Total (KZT):");

        Cell totalCell = totalRow.createCell(COL_AMOUNT_KZT);
        totalCell.setCellValue(report.totalsByCategory().get(categoryName).doubleValue());
        totalCell.setCellStyle(styles.get("total"));

        return currentRow + 1;
    }

    private int writeColumnHeaders(Sheet sheet, Map<String, CellStyle> styles, int currentRow) {
        Row headerRow = sheet.createRow(currentRow);
        String[] headers = {"Date", "Description", "Currency", "Amount", "Amount (KZT)"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("colHeader"));
        }

        return currentRow + 1;
    }

    private int writeTransactionRow(Sheet sheet, Map<String, CellStyle> styles, int currentRow,
                                    TransactionReportLine line) {
        Row row = sheet.createRow(currentRow);
        var transaction = line.transaction();

        Cell dateCell = row.createCell(COL_DATE);
        String date = transaction.getTransactionDate() != null
                ? transaction.getTransactionDate().toLocalDate().toString()
                : "N/A";
        dateCell.setCellValue(date);
        dateCell.setCellStyle(styles.get("data"));

        Cell descCell = row.createCell(COL_DESCRIPTION);
        descCell.setCellValue(transaction.getDescription());
        descCell.setCellStyle(styles.get("data"));

        Cell currencyCell = row.createCell(COL_CURRENCY);
        currencyCell.setCellValue(transaction.getCurrency());
        currencyCell.setCellStyle(styles.get("data"));

        Cell amountCell = row.createCell(COL_AMOUNT);
        amountCell.setCellValue(transaction.getAmount().doubleValue());
        amountCell.setCellStyle(styles.get("amount"));

        Cell kztCell = row.createCell(COL_AMOUNT_KZT);
        kztCell.setCellValue(line.amountInKzt().doubleValue());
        kztCell.setCellStyle(styles.get("amount"));

        return currentRow + 1;
    }

    private void writeGrandTotal(Sheet sheet, MonthlyReportData report, Map<String, CellStyle> styles, int currentRow) {
        Row grandTotalRow = sheet.createRow(currentRow);
        grandTotalRow.setHeightInPoints(18);

        for (int i = 0; i < TOTAL_COLUMNS; i++) {
            grandTotalRow.createCell(i).setCellStyle(styles.get("grandTotalLabel"));
        }

        grandTotalRow.getCell(0).setCellValue("Grand Total (KZT):");
        sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, TOTAL_COLUMNS - 2));

        Cell grandTotalCell = grandTotalRow.createCell(COL_AMOUNT_KZT);
        grandTotalCell.setCellValue(report.grandTotal().doubleValue());
        grandTotalCell.setCellStyle(styles.get("grandTotal"));
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < TOTAL_COLUMNS; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
        }
    }

    private void saveFile(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
    }
}
