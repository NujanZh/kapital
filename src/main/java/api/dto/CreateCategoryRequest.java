package api.dto;

public record CreateCategoryRequest(
        String name,
        String type
) {
}
