package data.service;

import data.repository.CategoryRepository;
import data.entity.Category;
import data.entity.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(String name, CategoryType type) {
        logger.debug("Attempting to create category: {} with type {}", name, type);

        Category category = Category.createNew(name.trim(), type);
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        logger.debug("Attempting to get all categories");
        return categoryRepository.findAll();
    }

    public Category getCategoryById(int id) {
        logger.debug("Attempting to get category with ID: {}", id);
        return categoryRepository.findById(id);
    }

    public boolean updateCategory(Category category) {
        logger.debug("Attempting to update category");

        if (category == null) {
            logger.warn("Update failed: category object is null");
            throw new IllegalArgumentException("Category cannot be null");
        }

        category.setName(category.getName().trim());

        return categoryRepository.update(category);
    }

    public void deleteCategory(int id) {
        logger.debug("Attempting to delete category with ID: {}", id);

        if (id <= 0) {
            logger.warn("Delete failed: invalid category ID: {}", id);
            throw new IllegalArgumentException("Category ID must be positive number");
        }

        categoryRepository.delete(id);
    }
}
