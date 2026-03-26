package data.dao;

import data.entity.Category;
import data.entity.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CategoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
    private final Connection connection;

    public CategoryDAO(Connection connection) {
        this.connection = connection;
    }

    public void save(Category category) {
        String sql = "INSERT INTO categories (name, type) VALUES (?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int columnIndex = 1;
                        category.setId(generatedKeys.getInt(columnIndex));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save category due to a database error", e);
        }

    }
    
    public List<Category> findAll() {
        var categories = new ArrayList<Category>();
        String sql = "SELECT id, name, type FROM categories";

        try (Statement stmt = connection.createStatement();
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
            logger.error("Error while getting categories: ", e);
        }

        return categories;
    }
    
    public boolean update(Category category) {
        if (category.getId() <= 0) {
            logger.warn("Attempt to update with invalid user ID: {}", category.getId());
            throw new IllegalArgumentException("Id must be positive number");
        }

        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

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

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Category with id: {}, deleted successfully", id);
            } else {
                logger.info("No category found with id: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error deleting category: {}", e.getMessage());
        }
    }
}
