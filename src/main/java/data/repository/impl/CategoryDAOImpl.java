package data.repository.impl;

import data.repository.CategoryRepository;
import data.entity.Category;
import data.entity.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class CategoryDAOImpl implements CategoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDAOImpl.class);
    private final Supplier<Connection> connectionProvider;

    public CategoryDAOImpl(Supplier<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void save(Category category) {
        String sql = "INSERT INTO categories (name, type) VALUES (?,?)";

        try (Connection connection = connectionProvider.get();
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType().name());

            logger.debug("Executing SQL: {} for category name={}", sql, category.getName());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getInt(1));
                    }
                }
            }

            logger.info("Category with name: {}, saved successfully", category.getName());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save category due to a database error", e);
        }

    }
    
    public List<Category> findAll() {
        var categories = new ArrayList<Category>();
        String sql = "SELECT id, name, type FROM categories";

        try (Connection connection = connectionProvider.get();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String typeString = rs.getString("type");
                CategoryType type = CategoryType.valueOf(typeString);

                Category t = new Category(
                        rs.getString("name"),
                        type
                );

                t.setId(rs.getInt("id"));

                categories.add(t);
            }

            logger.info("Successfully got {} categories from DB", categories.size());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get categories due to a database error", e);
        }

        return categories;
    }
    
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";

        try (Connection connection = connectionProvider.get();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType().name());
            pstmt.setInt(3, category.getId());

            logger.debug("Executing SQL: {} for category ID={}", sql, category.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Category with ID={} updated successfully", category.getId());
                return true;
            } else {
                logger.info("Category with ID={} not found in the database, update bypassed", category.getId());
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category due to a database error", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection connection = connectionProvider.get();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Category with id: {}, deleted successfully", id);
            } else {
                logger.info("No category found with id: {}", id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category due to a database error", e);
        }
    }
}
