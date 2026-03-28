package data.service;

import data.dao.CategoryDAO;
import data.entity.Category;
import data.entity.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public Category createCategory(String name, CategoryType type) {
        logger.debug("Attempting to create category: {} with type {}", name, type);

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Category creation failed: name is null or empty");
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (type == null) {
            logger.warn("Category creation failed: type is null");
            throw new IllegalArgumentException("Type cannot be null");
        }

        Category category = new Category(name.trim(), type);
        categoryDAO.save(category);

        logger.info("Category created successfully: {}", category);
        return category;
    }
}
