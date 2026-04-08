package data.repository;

import data.entity.Category;

import java.util.List;

public interface CategoryRepository {
    Category save(Category category);
    List<Category> findAll();
    Category findById(int id);
    boolean update(Category category);
    void delete(int id);
}
