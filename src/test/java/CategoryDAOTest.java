import data.dao.CategoryDAO;
import data.entity.Category;
import data.entity.CategoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CategoryDAOTest {

    private Connection connection;
    private CategoryDAO categoryDAO;

    @BeforeEach
    void setUp() throws SQLException {
        String url = "jdbc:h2:mem:testdb;MODE=MariaDB;INIT=RUNSCRIPT FROM 'classpath:database/init.sql';DB_CLOSE_DELAY=-1";
        connection = DriverManager.getConnection(url);
        categoryDAO = new CategoryDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Should successfully save a category and assign a generated ID")
    void shouldSaveCategory() {
        Category category = new Category("Food", CategoryType.EXPENSE);
        categoryDAO.save(category);
        assertThat(category.getId())
                .as("The generated ID should be greater than 0")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Should throw an exception when saving a category with a duplicate name")
    void shouldThrowExceptionWhenOnDuplicateName() {
        Category first = new Category("Food", CategoryType.EXPENSE);
        Category second = new Category("Food", CategoryType.EXPENSE);
        categoryDAO.save(first);
        assertThatThrownBy(() -> categoryDAO.save(second))
                .as("Database should prevent duplicate names via UNIQUE constraint")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should return all saved categories")
    void shouldFindAllCategories() {
        categoryDAO.save(new Category("Food", CategoryType.EXPENSE));
        categoryDAO.save(new Category("Transport", CategoryType.EXPENSE));

        List<Category> categories = categoryDAO.findAll();

        assertThat(categories)
                .hasSize(2)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Food", "Transport");
    }

    @Test
    @DisplayName("Should successfully update an existing category")
    void shouldUpdateCategory() {
        Category category = new Category("Food", CategoryType.EXPENSE);
        categoryDAO.save(category);

        category.setName("Fine Dining");
        boolean isUpdated = categoryDAO.update(category);

        assertThat(isUpdated).isTrue();

        Category found = categoryDAO.findAll().getFirst();
        assertThat(found.getName()).isEqualTo("Fine Dining");
    }

    @Test
    @DisplayName("Should return false when updateing a non-existent category")
    void shouldReturnFalseOnMissingCategory() {
        Category nonExistent = new Category("Non-existent", CategoryType.EXPENSE);
        nonExistent.setId(12345);

        boolean isUpdated = categoryDAO.update(nonExistent);

        assertThat(isUpdated).isFalse();
    }

    @Test
    @DisplayName("Should delete a category by its ID")
    void shouldDeleteCategory() {
        Category category = new Category("Food", CategoryType.EXPENSE);
        categoryDAO.save(category);
        categoryDAO.delete(category.getId());
        assertThat(categoryDAO.findAll()).isEmpty();
    }
}
