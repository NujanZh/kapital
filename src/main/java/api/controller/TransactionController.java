package api.controller;

import api.dto.CreateTransactionRequest;
import api.dto.UpdateTransactionRequest;
import data.entity.Category;
import data.entity.Transaction;
import data.service.CategoryService;
import data.service.TransactionService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TransactionController implements EndpointGroup {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public TransactionController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @Override
    public void addEndpoints() {
        path("/api/transactions", () -> {
            get(this::getAll);
            post(this::create);

            path("/monthly-expenses", () -> get(this::getMonthlyExpenses));
            path("/largest-expense", () -> get(this::getLargestExpense));
            path("/by-category", () -> get(this::getExpensesByCategory));
            path("/balance", () -> get(this::getBalance));

            path("/{id}", () -> {
                put(this::update);
                delete(this::deleteTransaction);
            });
        });
    }

    private void getAll(Context context) {
        List<Transaction> transactions = transactionService.getAllTransactions();
        context.json(transactions);
    }

    private void create(Context context) {
        CreateTransactionRequest request = context.bodyAsClass(CreateTransactionRequest.class);

        Category category = categoryService.getCategoryById(request.categoryId());
        Transaction transaction = transactionService.createTransaction(category, request.amount(), request.description(), request.currency());

        context
            .status(201)
            .json(transaction);
    }

    private void update(Context context) {
        int id = context.pathParamAsClass("id", Integer.class).get();

        UpdateTransactionRequest request = context.bodyAsClass(UpdateTransactionRequest.class);
        Category category = categoryService.getCategoryById(request.categoryId());
        Transaction transaction = Transaction.fromDatabase(id, category, request.amount(), request.description(), request.currency());

        boolean updated = transactionService.updateTransaction(transaction);

        if (updated) {
            context.json(transaction);
        } else {
            context.status(404).json(Map.of("error", "Transaction not found"));
        }
    }

    private void deleteTransaction(Context context) {
        int id = context.pathParamAsClass("id", Integer.class).get();
        transactionService.deleteTransaction(id);
        context.status(204);
    }

    private void getMonthlyExpenses(Context context) {
        try {
            int year = context.queryParamAsClass("year", Integer.class).get();
            String monthParam = context.queryParam("month");

            Month month;
            if (monthParam != null && monthParam.matches("\\d+")) {
                month = Month.of(Integer.parseInt(monthParam));
            } else {
                month = Month.valueOf(monthParam.toUpperCase());
            }

            BigDecimal total = transactionService.getExpensesForMonth(year, month);
            context.json(Map.of(
                    "year", year,
                    "month", month.toString(),
                    "totalExpenses", total
            ));
        } catch (Exception e) {
            context.status(400).json(Map.of("error", "Invalid request parameters. Please provide ?year=YYYY&month=MM"));
        }
    }

    private void getLargestExpense(Context context) {
        Optional<Transaction> largestExpense = transactionService.getLargestExpense();

        if (largestExpense.isPresent()) {
            context.json(largestExpense.get());
        } else {
            context.status(404).json(Map.of("error", "No expenses found"));
        }
    }

    private void getExpensesByCategory(Context context) {
        Map<String, BigDecimal> expenses = transactionService.getExpensesByCategory();
        context.json(expenses);
    }

    private void getBalance(Context context) {
        BigDecimal balance = transactionService.calculateCurrentBalance();
        context.json(Map.of("balance", balance));
    }
}
