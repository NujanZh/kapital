package data.dao.implementation;

import data.dao.repository.TransactionRepository;
import data.entity.Category;
import data.entity.CategoryType;
import data.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TransactionDAOImpl implements TransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDAOImpl.class);
    private final Supplier<Connection> connectionSupplier;

    public TransactionDAOImpl(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (category_id, amount, description) VALUES (?, ?, ?)";

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, transaction.getCategory().getId());
            pstmt.setBigDecimal(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getInt(1));
                    }
                }
            }

            logger.info("Transaction with id: {}, saved successfully", transaction.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction due to a database error", e);
        }
    }

    public List<Transaction> findAll() {
        var transactions = new ArrayList<Transaction>();
        String sql = "SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, c.id as cat_id, c.name, c.type " +
                "FROM transactions t " +
                "JOIN categories c ON c.id = t.category_id";

        try (Connection connection = connectionSupplier.get();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String categoryString = rs.getString("type");
                CategoryType categoryType = CategoryType.valueOf(categoryString);

                Category category = new Category(rs.getString("name"), categoryType);
                category.setId(rs.getInt("cat_id"));

                Transaction t = new Transaction(
                        category,
                        rs.getBigDecimal("amount"),
                        rs.getString("description")
                );

                t.setId(rs.getInt("id"));

                Timestamp timestamp = rs.getTimestamp("transaction_date");
                if (timestamp != null) {
                    t.setTransactionDate(timestamp.toLocalDateTime());
                }

                transactions.add(t);
            }

            logger.info("Successfully got {} transactions from DB", transactions.size());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions due to a database error", e);
        }

        return transactions;
    }

    public boolean update(Transaction transaction) {
        String sql = "UPDATE transactions SET amount = ?, description = ?, category_id = ? WHERE id = ?";

        try (Connection connection = connectionSupplier.get();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, transaction.getAmount());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setInt(3, transaction.getCategory().getId());
            pstmt.setInt(4, transaction.getId());

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
}
