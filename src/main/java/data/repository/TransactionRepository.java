package data.repository;

import data.entity.Transaction;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAll();
    boolean update(Transaction transaction);
    void delete(int id);

    BigDecimal getTotalExpensesByMonthAndYear(int year, Month month);
    Optional<Transaction> getLargestExpense();
    Map<String, BigDecimal> getExpensesByCategory();
    BigDecimal calculateBalance();
}
