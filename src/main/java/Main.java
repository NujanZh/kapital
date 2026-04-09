import api.controller.CategoryController;
import api.controller.ReportController;
import api.controller.TransactionController;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.DBConnection;
import data.currency.CurrencyConverter;
import data.currency.ExchangeRateClient;
import data.export.ExcelExporter;
import data.repository.impl.CategoryDAOImpl;
import data.repository.impl.TransactionDAOImpl;
import data.service.CategoryService;
import data.service.MonthlyReportService;
import data.service.TransactionService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Main {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("apple.awt.UIElement", "true");

        Supplier<Connection> connectionSupplier = () -> {
            try {
                return DBConnection.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to database", e);
            }
        };

        CategoryDAOImpl categoryRepository = new CategoryDAOImpl(connectionSupplier);
        TransactionDAOImpl transactionRepository = new TransactionDAOImpl(connectionSupplier);

        CategoryService categoryService = new CategoryService(categoryRepository);
        TransactionService transactionService = new TransactionService(transactionRepository);

        ExchangeRateClient exchangeRateClient = new ExchangeRateClient();
        CurrencyConverter currencyConverter = new CurrencyConverter(exchangeRateClient);
        MonthlyReportService monthlyReportService = new MonthlyReportService(transactionRepository, currencyConverter);
        ExcelExporter excelExporter = new ExcelExporter();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
            config.jsonMapper(
                new JavalinJackson().updateMapper(mapper -> {
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                })
            );
            config.routes.apiBuilder(() -> get("/", context -> context.redirect("/index.html")));
            config.routes.apiBuilder(new CategoryController(categoryService));
            config.routes.apiBuilder(new TransactionController(transactionService, categoryService));
            config.routes.apiBuilder(new ReportController(monthlyReportService, excelExporter));
        });

        app.start(7070);
    }
}
