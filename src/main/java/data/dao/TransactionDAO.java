package data.dao;

import data.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDAO.class);

    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (category_id, amount, description) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, transaction.getCategoryId());
            pstmt.setBigDecimal(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int columnIndex = 1;
                        transaction.setId(generatedKeys.getInt(columnIndex));
                    }
                }
            }

            logger.info("Transaction with id: {}, saved successfully", transaction.getId());
        } catch (SQLException e) {
            logger.error("Error saving transaction: {}", e.getMessage());
        }
    }

    public List<Transaction> findAll() {
        var transactions = new ArrayList<Transaction>();
        String sql = "SELECT id, category_id, amount, description, transaction_date FROM transactions";

        try (Connection connection = DBConnection.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("category_id"),
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
            logger.error("Error while getting transactions: ", e);
        }

        return transactions;
    }

    public boolean update(Transaction transaction) {
        if (transaction.getId() <= 0) {
            logger.warn("Attempt to update with invalid user ID: {}", transaction.getId());
            throw new IllegalArgumentException("Id must be positive number");
        }

        String sql = "UPDATE transactions SET amount = ?, description = ?, category_id = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, transaction.getAmount());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setInt(3, transaction.getCategoryId());
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

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Transaction with id: {}, deleted successfully", id);
            } else {
                logger.info("No transaction found with id: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error deleting transaction: {}", e.getMessage());
        }
    }
}
