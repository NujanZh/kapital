package data.export;

import data.currency.CurrencyConverter;
import data.entity.Transaction;
import data.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        Map<String, BigDecimal> rateCache = new HashMap<>();
        List<Transaction> transactions = transactionRepository.getTransactionsByMonthAndYear(year, month);
        Map<String, List<TransactionReportLine>> transactionsByCategory = new HashMap<>();
        Map<String, BigDecimal> totalsByCategory = new HashMap<>();

        transactions.forEach(transaction -> {
            String categoryName = transaction.getCategory().getName();
            BigDecimal amountInKzt = getConvertedAmount(transaction, rateCache);
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

    private BigDecimal getConvertedAmount(Transaction transaction, Map<String, BigDecimal> rateCache) {
        String currency = transaction.getCurrency();

        if (currency.equals("KZT")) {
            return transaction.getAmount();
        }

        BigDecimal rate = rateCache.computeIfAbsent(
                currency,
                c -> currencyConverter.getRate(c, "KZT")
        );

        return transaction.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
