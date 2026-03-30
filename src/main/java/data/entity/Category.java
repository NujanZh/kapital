package data.entity;

public class Category {
    private int id;
    private String name;
    private CategoryType type;



    public Category(String name, CategoryType type) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        if (type == null) throw new IllegalArgumentException("Type cannot be null");

        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }
}
