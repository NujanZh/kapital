package data.service;

import data.repository.TransactionRepository;
import data.entity.Category;
import data.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(Category category, BigDecimal amount, String description, String currency) {
        logger.debug("Attempting to create new transaction");

        Transaction transaction = Transaction.createNew(category, amount, description, currency);
        return transactionRepository.save(transaction);
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
        return transactionRepository.getTotalExpensesByMonthAndYear(year, month);
    }

    public Optional<Transaction> getLargestExpense() {
        return transactionRepository.getLargestExpense();
    }

    public Map<String, BigDecimal> getExpensesByCategory() {
        return transactionRepository.getExpensesByCategory();
    }

    public BigDecimal calculateCurrentBalance() {
        return transactionRepository.calculateBalance();
    }
}
