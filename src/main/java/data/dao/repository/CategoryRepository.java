package data.dao.repository;

import data.entity.Category;

import java.util.List;

public interface CategoryRepository {
    void save(Category category);
    List<Category> findAll();
    boolean update(Category category);
    void delete(int id);
}
