package data.repository;

import data.entity.Category;

import java.util.List;

public interface CategoryRepository {
    Category save(Category category);
    List<Category> findAll();
    boolean update(Category category);
    void delete(int id);
}
