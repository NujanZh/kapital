package data.service;

import data.dao.implementation.CategoryDAOImpl;
import data.entity.Category;
import data.entity.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryDAOImpl categoryDAOImpl;

    public CategoryService(CategoryDAOImpl categoryDAOImpl) {
        this.categoryDAOImpl = categoryDAOImpl;
    }

    public Category createCategory(String name, CategoryType type) {
        logger.debug("Attempting to create category: {} with type {}", name, type);

        Category category = new Category(name.trim(), type);
        categoryDAOImpl.save(category);

        logger.info("Category created successfully: {}", category);
        return category;
    }

    public List<Category> getAllCategories() {
        logger.debug("Attempting to get all categories");
        return categoryDAOImpl.findAll();
    }

    public boolean updateCategory(Category category) {
        logger.debug("Attempting to update category");

        if (category == null) {
            logger.warn("Update failed: category object is null");
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getId() <= 0) {
            logger.warn("Update failed: invalid category ID: {}", category.getId());
            throw new IllegalArgumentException("Category ID must be positive number");
        }

        category.setName(category.getName().trim());

        return categoryDAOImpl.update(category);
    }

    public void deleteCategory(int id) {
        logger.debug("Attempting to delete category with ID: {}", id);

        if (id <= 0) {
            logger.warn("Delete failed: invalid category ID: {}", id);
            throw new IllegalArgumentException("Category ID must be positive number");
        }

        categoryDAOImpl.delete(id);
    }
}
