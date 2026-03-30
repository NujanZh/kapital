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
    private CategoryDAOImpl categoryDAO;

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
        categoryDAO = new CategoryDAOImpl(connectionSupplier);
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
        Category category = new Category("Food", CategoryType.EXPENSE);
        categoryDAO.save(category);


        Transaction saved = transactionDAOImpl.save(Transaction.createNew(category, new BigDecimal("10.00"), "Test"));

        assertThat(saved.getId())
                .as("The generated ID should be greater than 0")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Should return all saved transactions")
    void shouldFindAllTransactions() {
        Category category = new Category("Groceries", CategoryType.EXPENSE);
        categoryDAO.save(category);

        transactionDAOImpl.save(Transaction.createNew(category, new BigDecimal("10.00"), "Test"));
        transactionDAOImpl.save(Transaction.createNew(category, new BigDecimal("10.00"), "Test2"));

        List<Transaction> categories = transactionDAOImpl.findAll();

        assertThat(categories)
                .hasSize(2)
                .extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Test", "Test2");
    }

    @Test
    @DisplayName("Should successfully update an existing transaction")
    void shouldUpdateTransaction() {
        Category category = new Category("Entertainment", CategoryType.EXPENSE);
        categoryDAO.save(category);

        Transaction saved = transactionDAOImpl.save(Transaction.createNew(category, new BigDecimal("10.00"), "Movie ticket"));

        saved.setAmount(new BigDecimal("20.00"));
        saved.setDescription("Movie ticket (For two persons)");

        boolean isUpdated = transactionDAOImpl.update(saved);

        assertThat(isUpdated).isTrue();

        Transaction found = transactionDAOImpl.findAll().getFirst();
        assertThat(found.getAmount()).isEqualByComparingTo("20.00");
        assertThat(found.getDescription()).isEqualTo("Movie ticket (For two persons)");
    }

    @Test
    @DisplayName("Should return false when updating a non-existent transaction")
    void shouldReturnFalseOnMissingTransaction() {
        Category category = new Category("Transport", CategoryType.EXPENSE);
        categoryDAO.save(category);

        Transaction nonExistent = Transaction.fromDatabase(12345, category, new BigDecimal("10.00"), "Bus ticket");
        boolean isUpdated = transactionDAOImpl.update(nonExistent);

        assertThat(isUpdated).isFalse();
    }

    @Test
    @DisplayName("Should delete a transaction by its ID")
    void shouldDeleteTransaction() {
        Category category = new Category("Utilities", CategoryType.EXPENSE);
        categoryDAO.save(category);

        Transaction saved = transactionDAOImpl.save(Transaction.createNew(category, new BigDecimal("10.00"), "Electricity bill"));
        transactionDAOImpl.delete(saved.getId());

        assertThat(transactionDAOImpl.findAll()).isEmpty();
    }
}
