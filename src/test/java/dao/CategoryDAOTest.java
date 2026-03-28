package dao;

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
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class CategoryDAOTest {

    private Connection keepAliveConnection;
    private CategoryDAO categoryDAO;

    @BeforeEach
    void setUp() throws SQLException {
        // 1. URL С ИНИЦИАЛИЗАЦИЕЙ (INIT)
        // Создаем первичное подключение к in-memory базе H2.
        // Здесь мы специально используем параметр INIT=RUNSCRIPT, чтобы при установке соединения
        // выполнился скрипт init.sql (который делает DROP TABLE IF EXISTS и CREATE TABLE).
        // Это подключение (keepAliveConnection) мы сохраняем и не закрываем до конца теста (tearDown),
        // чтобы база данных (mem:testdb) "жила" в оперативной памяти и не удалилась.
        String initUrl = "jdbc:h2:mem:testdb;MODE=MariaDB;INIT=RUNSCRIPT FROM 'classpath:database/init.sql';DB_CLOSE_DELAY=-1";
        keepAliveConnection = DriverManager.getConnection(initUrl);

        // 2. ЧИСТЫЙ URL ДЛЯ РАБОТЫ DAO (БЕЗ INIT)
        // Почему это важно: DAO каждый раз запрашивает новое подключение (вызывает Supplier)
        // для выполнения SQL-запросов (save, findAll, update).
        // Если бы мы передали сюда URL с параметром INIT, то перед каждым отдельным запросом
        // H2 заново бы выполняла скрипт init.sql. То есть база бы сбрасывалась и удаляла все данные,
        // которые мы сохранили строчкой выше. Тесты бы падали из-за того, что ищут данные в пустой БД.
        String daoUrl = "jdbc:h2:mem:testdb;MODE=MariaDB;DB_CLOSE_DELAY=-1";

        Supplier<Connection> connectionSupplier = () -> {
            try {
                return DriverManager.getConnection(daoUrl);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to H2 in tests", e);
            }
        };

        categoryDAO = new CategoryDAO(connectionSupplier);
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
