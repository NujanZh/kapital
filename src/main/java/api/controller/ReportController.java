package api.controller;

import data.export.ExcelExporter;
import data.export.MonthlyReportData;
import data.service.MonthlyReportService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class ReportController implements EndpointGroup {

    private final MonthlyReportService reportService;
    private final ExcelExporter excelExporter;

    public ReportController(MonthlyReportService reportService, ExcelExporter excelExporter) {
        this.reportService = reportService;
        this.excelExporter = excelExporter;
    }

    @Override
    public void addEndpoints() {
        path("/api/reports", () -> {
            get("/monthly", this::downloadMonthlyReport);
        });
    }

    private void downloadMonthlyReport(Context context) {
        try {
            int year = context.queryParamAsClass("year", Integer.class).get();
            String monthParam = context.queryParam("month");

            Month month;
            if (monthParam != null && monthParam.matches("\\d+")) {
                month = Month.of(Integer.parseInt(monthParam));
            } else {
                month = Month.valueOf(monthParam.toUpperCase());
            }

            MonthlyReportData reportData = reportService.buildReport(year, month);

            Path tempFile = Files.createTempFile("monthly_report_" + year + "_" + month, ".xlsx");

            excelExporter.export(reportData, tempFile.toString());

            byte[] fileBytes = Files.readAllBytes(tempFile);
            Files.delete(tempFile);

            String filename = String.format("expenses_report_%d_%s.xlsx", year, month.toString().toLowerCase());

            context.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            context.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            context.result(fileBytes);

        } catch (Exception e) {
            context.status(500).json(Map.of(
                    "error", "Failed to generate report.",
                    "details", e.getMessage()
            ));
        } catch (Throwable t) {
            context.status(500).json(Map.of(
                    "error", "Failed to generate report.",
                    "details", t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()
            ));
        }
    }
}
