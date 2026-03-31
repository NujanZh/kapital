package data.entity;

import java.util.Objects;

public class Category {
    private final int id;
    private String name;
    private CategoryType type;

    private Category(int id, String name, CategoryType type) {
        validate(name, type);
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static Category createNew(String name, CategoryType type) {
        return new Category(0, name, type);
    }

    public static Category fromDatabase(int id, String name, CategoryType type) {
        if (id <= 0) throw new IllegalArgumentException("Database category must have a positive ID");
        return new Category(id, name, type);
    }

    private static void validate(String name, CategoryType type) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        if (type == null) throw new IllegalArgumentException("Type cannot be null");
    }

    public int getId() {
        return id;
    }

    public boolean isPersisted() {
        return id > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        if (type == null) throw new IllegalArgumentException("Type cannot be null");
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id && Objects.equals(name, category.name) && type == category.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }
}
