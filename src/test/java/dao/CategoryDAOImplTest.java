package dao;

import data.repository.impl.CategoryDAOImpl;
import data.entity.Category;
import data.entity.CategoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class CategoryDAOImplTest {

    private Connection keepAliveConnection;
    private CategoryDAOImpl categoryDAOImpl;

    @BeforeEach
    void setUp() throws SQLException {
        String initUrl = "jdbc:h2:mem:testdb;MODE=MariaDB;INIT=RUNSCRIPT FROM 'classpath:database/init.sql';DB_CLOSE_DELAY=-1";
        keepAliveConnection = DriverManager.getConnection(initUrl);

        String daoUrl = "jdbc:h2:mem:testdb;MODE=MariaDB;DB_CLOSE_DELAY=-1";

        Supplier<Connection> connectionSupplier = () -> {
            try {
                return DriverManager.getConnection(daoUrl);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to H2 in tests", e);
            }
        };

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
    @DisplayName("Should successfully save a category and assign a generated ID")
    void shouldSaveCategory() {
        Category saved = categoryDAOImpl.save(Category.createNew("Food", CategoryType.EXPENSE));

        assertThat(saved.getId())
                .as("The generated ID should be greater than 0")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Should throw an exception when saving a category with a duplicate name")
    void shouldThrowExceptionWhenOnDuplicateName() {
        Category first = Category.createNew("Food", CategoryType.EXPENSE);
        Category second = Category.createNew("Food", CategoryType.EXPENSE);

        categoryDAOImpl.save(first);

        assertThatThrownBy(() -> categoryDAOImpl.save(second))
                .as("Database should prevent duplicate names via UNIQUE constraint")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should return all saved categories")
    void shouldFindAllCategories() {
        categoryDAOImpl.save(Category.createNew("Food", CategoryType.EXPENSE));
        categoryDAOImpl.save(Category.createNew("Transport", CategoryType.EXPENSE));

        List<Category> categories = categoryDAOImpl.findAll();

        assertThat(categories)
                .hasSize(2)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Food", "Transport");
    }

    @Test
    @DisplayName("Should successfully update an existing category")
    void shouldUpdateCategory() {
        Category saved = categoryDAOImpl.save(Category.createNew("Food", CategoryType.EXPENSE));

        saved.setName("Fine Dining");
        boolean isUpdated = categoryDAOImpl.update(saved);

        assertThat(isUpdated).isTrue();

        Category found = categoryDAOImpl.findAll().getFirst();
        assertThat(found.getName()).isEqualTo("Fine Dining");
    }

    @Test
    @DisplayName("Should return false when updating a non-existent category")
    void shouldReturnFalseOnMissingCategory() {
        Category nonExistent = Category.fromDatabase(12345, "Non-existent", CategoryType.EXPENSE);
        boolean isUpdated = categoryDAOImpl.update(nonExistent);

        assertThat(isUpdated).isFalse();
    }

    @Test
    @DisplayName("Should delete a category by its ID")
    void shouldDeleteCategory() {
        Category saved = categoryDAOImpl.save(Category.createNew("Food", CategoryType.EXPENSE));
        categoryDAOImpl.delete(saved.getId());

        assertThat(categoryDAOImpl.findAll()).isEmpty();
    }
}
