import data.DBConnection;
import data.currency.CurrencyConverter;
import data.currency.ExchangeRateClient;
import data.export.ExcelExporter;
import data.export.MonthlyReportData;
import data.service.MonthlyReportService;
import data.repository.impl.TransactionDAOImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Month;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        ExcelExporter exporter = new ExcelExporter();
        Supplier<Connection> connectionSupplier = () -> {
            try {
                return DBConnection.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to mariaDB in main", e);
            }
        };
        TransactionDAOImpl transactionRepository = new TransactionDAOImpl(connectionSupplier);
        ExchangeRateClient client = new ExchangeRateClient();
        CurrencyConverter converter = new CurrencyConverter(client);
        MonthlyReportService service = new MonthlyReportService(transactionRepository, converter);
        MonthlyReportData report = service.buildReport(2023, Month.JANUARY);
        exporter.export(report, "monthly-report.xlsx");
    }
}
