package data.export;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;

public class ExcelExporter {
    public void export(MonthlyReportData report, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            CellStyle style = createStyle();

            int currentRow = 0;
            currentRow = writeHeader(sheet, report, style, currentRow);
            currentRow = writeCategories(sheet, report, style, currentRow);
            writeGrandTotal(sheet, report, style, currentRow);

            autoSizeColumns(sheet);
            saveFile(workbook, filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }

    private CellStyle createStyle() {

    }

    private int writeHeader(Sheet sheet, MonthlyReportData report, CellStyle style, int currentRow) {

    }

    private int writeCategories(Sheet sheet, MonthlyReportData report, CellStyle style, int currentRow) {

    }

    private void writeCategory() {
    }

    private int writeGrandTotal(Sheet sheet, MonthlyReportData report, CellStyle style, int currentRow) {
    }

    private void autoSizeColumns(Sheet sheet) {}

    private void saveFile(Workbook workbook, String filePath) throws IOException {}
}
