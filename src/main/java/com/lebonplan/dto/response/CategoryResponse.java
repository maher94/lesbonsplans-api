package com.lebonplan.dto.response;

import com.lebonplan.entity.Category;

import java.util.List;

public record CategoryResponse(
    Integer id,
    String name,
    String slug,
    String icon,
    String description,
    Integer parentId,
    Integer position,
    boolean isActive,
    List<CategoryResponse> children
) {
    // Conversion sans enfants (pour les listes plates)
    public static CategoryResponse from(Category cat) {
        return new CategoryResponse(
            cat.getId(),
            cat.getName(),
            cat.getSlug(),
            cat.getIcon(),
            cat.getDescription(),
            cat.getParent() != null ? cat.getParent().getId() : null,
            cat.getPosition(),
            cat.isActive(),
            null
        );
    }

    // Conversion avec enfants (pour l'arbre complet)
    public static CategoryResponse fromWithChildren(Category cat) {
        List<CategoryResponse> children = cat.getChildren().stream()
            .filter(Category::isActive)
            .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
            .map(CategoryResponse::from)
            .toList();

        return new CategoryResponse(
            cat.getId(),
            cat.getName(),
            cat.getSlug(),
            cat.getIcon(),
            cat.getDescription(),
            cat.getParent() != null ? cat.getParent().getId() : null,
            cat.getPosition(),
            cat.isActive(),
            children.isEmpty() ? null : children
        );
    }
}
