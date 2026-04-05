package data.export;

import data.currency.CurrencyConverter;
import data.entity.Transaction;
import data.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyReportService {

    private final TransactionRepository transactionRepository;
    private final CurrencyConverter currencyConverter;

    public MonthlyReportService(TransactionRepository transactionRepository, CurrencyConverter currencyConverter) {
        this.transactionRepository = transactionRepository;
        this.currencyConverter = currencyConverter;
    }

    public MonthlyReportData buildReport(int year, Month month) {
        List<Transaction> transactions = transactionRepository.getTransactionsByMonthAndYear(year, month);
        Map<String, List<TransactionReportLine>> transactionsByCategory = new HashMap<>();
        Map<String, BigDecimal> totalsByCategory = new HashMap<>();

        transactions.forEach(transaction -> {
            String categoryName = transaction.getCategory().getName();
            BigDecimal amountInKzt = currencyConverter.convert(transaction.getCurrency(), "KZT", transaction.getAmount());
            TransactionReportLine reportLine = new TransactionReportLine(transaction, amountInKzt);

            transactionsByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(reportLine);
            totalsByCategory.merge(categoryName, amountInKzt, BigDecimal::add);
        });

        BigDecimal grandTotal = transactionsByCategory.values().stream()
                .flatMap(List::stream)
                .map(TransactionReportLine::amountInKzt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyReportData(year, month, transactionsByCategory, totalsByCategory, grandTotal);
    }
}
