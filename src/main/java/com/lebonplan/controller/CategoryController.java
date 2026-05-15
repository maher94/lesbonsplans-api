package com.lebonplan.controller;

import com.lebonplan.dto.request.CategoryRequest;
import com.lebonplan.dto.response.CategoryResponse;
import com.lebonplan.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Toutes les catégories parentes + sous-catégories imbriquées.
     * Utilisé par le frontend pour les filtres et menus.
     * PUBLIC
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAllWithChildren());
    }

    /**
     * GET /api/categories/flat
     * Toutes les catégories à plat (sans arbre).
     * Utile pour les selects de formulaire.
     * PUBLIC
     */
    @GetMapping("/flat")
    public ResponseEntity<List<CategoryResponse>> getAllFlat() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    /**
     * GET /api/categories/{id}
     * Une catégorie par son ID avec ses enfants.
     * PUBLIC
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * GET /api/categories/slug/{slug}
     * Une catégorie par son slug (ex: "evenements").
     * PUBLIC
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    /**
     * GET /api/categories/{id}/children
     * Sous-catégories d'un parent.
     * PUBLIC
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getChildren(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getChildren(id));
    }

    /**
     * POST /api/categories
     * Créer une catégorie.
     * ADMIN uniquement
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    /**
     * PUT /api/categories/{id}
     * Modifier une catégorie.
     * ADMIN uniquement
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    /**
     * DELETE /api/categories/{id}
     * Désactiver une catégorie (soft delete).
     * ADMIN uniquement
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
