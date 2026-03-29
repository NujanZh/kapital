package data.service;

import data.repository.TransactionRepository;
import data.entity.Category;
import data.entity.CategoryType;
import data.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(Category category, BigDecimal amount, String description) {
        logger.debug("Attempting to create new transaction");

        Transaction transaction = new Transaction(category, amount, description);
        transactionRepository.save(transaction);
        return transaction;
    }

    public List<Transaction> getAllTransactions() {
        logger.debug("Attempting to get all transactions");
        return transactionRepository.findAll();
    }

    public boolean updateTransaction(Transaction transaction) {
        logger.debug("Attempting to update transaction");

        if (transaction == null) {
            logger.warn("Update failed: transaction object is null");
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (transaction.getId() <= 0) {
            logger.warn("Update failed: invalid transaction ID: {}", transaction.getId());
            throw new IllegalArgumentException("Transaction ID must be positive number");
        }

        return transactionRepository.update(transaction);
    }

    public void deleteTransaction(int id) {
        logger.debug("Attempting to delete transaction with ID: {}", id);

        if (id <= 0) {
            logger.warn("Delete failed: invalid transaction ID: {}", id);
            throw new IllegalArgumentException("Transaction ID must be positive number");
        }

        transactionRepository.delete(id);
    }

    public BigDecimal getExpensesForMonth(int year, Month month) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .filter(t -> t.getTransactionDate() != null)
                .filter(t -> t.getTransactionDate().getYear() == year)
                .filter(t -> t.getTransactionDate().getMonth() == month)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Transaction> getLargestExpense() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .max(Comparator.comparing(Transaction::getAmount));
    }

    public Map<String, BigDecimal> getExpensesByCategory() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    public BigDecimal calculateCurrentBalance() {
        return transactionRepository.findAll().stream()
                .map(t -> t.getCategory().getType() == CategoryType.INCOME ?
                        t.getAmount() :
                        t.getAmount().negate()
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
