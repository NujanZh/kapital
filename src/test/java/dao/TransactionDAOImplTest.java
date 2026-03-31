package dao;

import data.repository.impl.CategoryDAOImpl;
import data.repository.impl.TransactionDAOImpl;
import data.entity.Category;
import data.entity.CategoryType;
import data.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionDAOImplTest {

    private Connection keepAliveConnection;
    private TransactionDAOImpl transactionDAOImpl;
    private CategoryDAOImpl categoryDAOImpl;

    @BeforeEach
    void setUp() throws SQLException {
        String initUrl = "jdbc:h2:mem:testdb;MODE=MariaDB;INIT=RUNSCRIPT FROM 'classpath:database/init.sql';DB_CLOSE_DELAY=-1";
        keepAliveConnection = DriverManager.getConnection(initUrl);

        String daoUrl = "jdbc:h2:mem:testdb;MODE=MariaDB;DB_CLOSE_DELAY=-1";

        Supplier<Connection> connectionSupplier = () -> {
            try {
                return DriverManager.getConnection(daoUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to connect to H2 in tests", e);
            }
        };

        transactionDAOImpl = new TransactionDAOImpl(connectionSupplier);
        categoryDAOImpl = new CategoryDAOImpl(connectionSupplier);
    }
    
    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = keepAliveConnection.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM categories");
        }
        if (keepAliveConnection != null && !keepAliveConnection.isClosed()) {
            keepAliveConnection.close();
        }
    }

    @Test
    @DisplayName("Should successfully save a transaction and assign a generated ID")
    void shouldSaveTransaction() {
        Category savedCategory = categoryDAOImpl.save(Category.createNew("Food", CategoryType.EXPENSE));

        Transaction savedTransaction = transactionDAOImpl.save(Transaction.createNew(savedCategory, new BigDecimal("10.00"), "Test"));

        assertThat(savedTransaction.getId())
                .as("The generated ID should be greater than 0")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Should return all saved transactions")
    void shouldFindAllTransactions() {
        Category savedCategory = categoryDAOImpl.save(Category.createNew("Groceries", CategoryType.EXPENSE));

        transactionDAOImpl.save(Transaction.createNew(savedCategory, new BigDecimal("10.00"), "Test"));
        transactionDAOImpl.save(Transaction.createNew(savedCategory, new BigDecimal("10.00"), "Test2"));

        List<Transaction> transactions = transactionDAOImpl.findAll();

        assertThat(transactions)
                .hasSize(2)
                .extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Test", "Test2");
    }

    @Test
    @DisplayName("Should successfully update an existing transaction")
    void shouldUpdateTransaction() {
        Category savedCategory = categoryDAOImpl.save(Category.createNew("Entertainment", CategoryType.EXPENSE));

        Transaction savedTransaction = transactionDAOImpl.save(Transaction.createNew(savedCategory, new BigDecimal("10.00"), "Movie ticket"));

        savedTransaction.setAmount(new BigDecimal("20.00"));
        savedTransaction.setDescription("Movie ticket (For two persons)");

        boolean isUpdated = transactionDAOImpl.update(savedTransaction);

        assertThat(isUpdated).isTrue();

        Transaction found = transactionDAOImpl.findAll().getFirst();
        assertThat(found.getAmount()).isEqualByComparingTo("20.00");
        assertThat(found.getDescription()).isEqualTo("Movie ticket (For two persons)");
    }

    @Test
    @DisplayName("Should return false when updating a non-existent transaction")
    void shouldReturnFalseOnMissingTransaction() {
        Category savedCategory = categoryDAOImpl.save(Category.createNew("Transport", CategoryType.EXPENSE));

        Transaction nonExistent = Transaction.fromDatabase(12345, savedCategory, new BigDecimal("10.00"), "Bus ticket");
        boolean isUpdated = transactionDAOImpl.update(nonExistent);

        assertThat(isUpdated).isFalse();
    }

    @Test
    @DisplayName("Should delete a transaction by its ID")
    void shouldDeleteTransaction() {
        Category savedCategory = categoryDAOImpl.save(Category.createNew("Utilities", CategoryType.EXPENSE));

        Transaction savedTransaction = transactionDAOImpl.save(Transaction.createNew(savedCategory, new BigDecimal("10.00"), "Electricity bill"));
        transactionDAOImpl.delete(savedTransaction.getId());

        assertThat(transactionDAOImpl.findAll()).isEmpty();
    }
}
