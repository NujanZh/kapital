package data.repository.impl;

import data.repository.TransactionRepository;
import data.entity.Category;
import data.entity.CategoryType;
import data.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Month;
import java.util.*;
import java.util.function.Supplier;

public class TransactionDAOImpl implements TransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDAOImpl.class);
    private final Supplier<Connection> connectionSupplier;

    public TransactionDAOImpl(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Transaction save(Transaction transaction) {
        String sql = "INSERT INTO transactions (category_id, amount, description, currency) VALUES (?, ?, ?, ?)";

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, transaction.getCategory().getId());
            pstmt.setBigDecimal(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getCurrency());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);

                        return Transaction.fromDatabase(
                                generatedId,
                                transaction.getCategory(),
                                transaction.getAmount(),
                                transaction.getDescription(),
                                transaction.getCurrency()
                        );
                    }
                }
            }

            logger.info("Transaction with id: {}, saved successfully", transaction.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction due to a database error", e);
        }

        throw new RuntimeException("Save succeeded but no ID was generated");
    }

    @Override
    public List<Transaction> findAll() {
        var transactions = new ArrayList<Transaction>();
        String sql = "SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, t.currency, c.id as cat_id, c.name, c.type " +
                "FROM transactions t " +
                "JOIN categories c ON c.id = t.category_id";

        try (Connection connection = connectionSupplier.get();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = mapRowToTransaction(rs);
                transactions.add(t);
            }

            logger.info("Successfully got {} transactions from DB", transactions.size());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions due to a database error", e);
        }

        return transactions;
    }

    @Override
    public Transaction findById(int id) {
        String sql = """
                SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, t.currency, c.id as cat_id, c.name, c.type 
                FROM transactions t 
                JOIN categories c ON c.id = t.category_id 
                    WHERE t.id = ?
                """;

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmp = connection.prepareStatement(sql)
        ) {
            pstmp.setInt(1, id);

            try (ResultSet rs = pstmp.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transaction due to a database error", e);
        }

        throw new RuntimeException("No transaction found with id: " + id);
    }

    @Override
    public boolean update(Transaction transaction) {
        String sql = "UPDATE transactions SET amount = ?, description = ?, currency = ?, category_id = ? WHERE id = ?";

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, transaction.getAmount());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setString(3, transaction.getCurrency());
            pstmt.setInt(4, transaction.getCategory().getId());
            pstmt.setInt(5, transaction.getId());

            logger.debug("Executing SQL: {} for transaction ID={}", sql, transaction.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Transaction with ID={} updated successfully", transaction.getId());
                return true;
            } else {
                logger.info("Transaction with ID={} not found in the database, update bypassed", transaction.getId());
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction due to a database error", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Transaction with id: {}, deleted successfully", id);
            } else {
                logger.info("No transaction found with id: {}", id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction due to a database error", e);
        }
    }

    @Override
    public BigDecimal getTotalExpensesByMonthAndYear(int year, Month month) {
        String sql = """
                SELECT COALESCE(SUM(t.amount), 0)
                FROM transactions t
                JOIN categories c ON c.id = t.category_id
                WHERE c.type = 'EXPENSE'
                    AND YEAR(t.transaction_date) = ?
                    AND MONTH(t.transaction_date) = ?
                """;

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month.getValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total expense by month and year due to a database error", e);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public List<Transaction> getTransactionsByMonthAndYear(int year, Month month) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
                SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, t.currency, c.id as cat_id, c.name, c.type
                FROM transactions t
                JOIN categories c ON c.id = t.category_id
                WHERE c.type = 'EXPENSE'
                    AND YEAR(t.transaction_date) = ?
                    AND MONTH(t.transaction_date) = ?
                """;

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month.getValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions by month and year due to a database error", e);
        }

        return transactions;
    }

    @Override
    public Optional<Transaction> getLargestExpense() {
        String sql = """
                SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, t.currency,
                       c.id as cat_id, c.name, c.type
                FROM transactions t
                JOIN categories c ON c.id = t.category_id
                WHERE c.type = 'EXPENSE'
                ORDER BY t.amount DESC
                LIMIT 1
                """;

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapRowToTransaction(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get largest expense due to a database error", e);
        }

        return Optional.empty();
    }

    @Override
    public Map<String, BigDecimal> getExpensesByCategory() {
        String sql = """
                SELECT c.name, COALESCE(SUM(t.amount), 0) as total
                FROM transactions t
                JOIN categories c ON c.id = t.category_id
                WHERE c.type = 'EXPENSE'
                GROUP BY c.name
                """;

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("name"), rs.getBigDecimal("total"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get expense by category due to a database error", e);
        }

        return result;
    }

    @Override
    public BigDecimal calculateBalance() {
        String sql = """
                SELECT COALESCE(SUM(
                    IF(c.type = 'INCOME', t.amount, -t.amount)
                ), 0)
                FROM transactions t
                JOIN categories c ON c.id = t.category_id
                """;

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate balance due to a database error", e);
        }

        return BigDecimal.ZERO;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        CategoryType categoryType = CategoryType.valueOf(rs.getString("type"));
        Category category = Category.fromDatabase(rs.getInt("cat_id"), rs.getString("name"), categoryType);

        Transaction t = Transaction.fromDatabase(
                rs.getInt("id"),
                category,
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getString("currency")
        );

        Timestamp timestamp = rs.getTimestamp("transaction_date");
        if (timestamp != null) {
            t.setTransactionDate(timestamp.toLocalDateTime());
        }

        return t;
    }
}
