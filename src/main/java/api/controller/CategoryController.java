package api.controller;

import api.dto.CreateCategoryRequest;
import api.dto.UpdateCategoryRequest;
import data.entity.Category;
import data.entity.CategoryType;
import data.service.CategoryService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CategoryController implements EndpointGroup {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void addEndpoints() {
        path("/api/categories", () -> {
            get(this::getAll);
            post(this::create);
            path("/{id}", () -> {
                put(this::update);
                delete(this::deleteCategory);
            });
        });
    }

    private void getAll(Context context) {
        List<Category> categories = categoryService.getAllCategories();
        context.json(categories);
    }

    private void create(Context context) {
        CreateCategoryRequest request = context.bodyAsClass(CreateCategoryRequest.class);

        CategoryType type = CategoryType.valueOf(request.type().toUpperCase());
        Category result = categoryService.createCategory(request.name(), type);

        context
            .status(201)
            .json(result);
    }

    private void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        UpdateCategoryRequest request = ctx.bodyAsClass(UpdateCategoryRequest.class);

        CategoryType type = CategoryType.valueOf(request.type().toUpperCase());
        Category category = Category.fromDatabase(id, request.name(), type);

        boolean updated = categoryService.updateCategory(category);

        if (updated) {
            ctx.json(category);
        } else {
            ctx.status(404).json(Map.of("error", "Category not found"));
        }
    }

    private void deleteCategory(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        categoryService.deleteCategory(id);
        ctx.status(204);
    }
}
