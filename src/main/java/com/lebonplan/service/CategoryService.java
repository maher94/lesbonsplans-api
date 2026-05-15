package com.lebonplan.service;

import com.lebonplan.dto.request.CategoryRequest;
import com.lebonplan.dto.response.CategoryResponse;
import com.lebonplan.entity.Category;
import com.lebonplan.exception.ResourceNotFoundException;
import com.lebonplan.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ── Lecture ──────────────────────────────────────────────

    /**
     * Retourne toutes les catégories parentes avec leurs sous-catégories imbriquées.
     * Utilisé par le frontend pour afficher les filtres et menus.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllWithChildren() {
        return categoryRepository
            .findByParentIsNullAndIsActiveTrueOrderByPositionAsc()
            .stream()
            .map(CategoryResponse::fromWithChildren)
            .toList();
    }

    /**
     * Retourne toutes les catégories actives à plat (sans arbre).
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository
            .findByIsActiveTrueOrderByPositionAsc()
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    /**
     * Retourne une catégorie par son ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getById(Integer id) {
        Category cat = findOrThrow(id);
        return CategoryResponse.fromWithChildren(cat);
    }

    /**
     * Retourne une catégorie par son slug.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category cat = categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + slug));
        return CategoryResponse.fromWithChildren(cat);
    }

    /**
     * Retourne les sous-catégories d'un parent.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildren(Integer parentId) {
        findOrThrow(parentId); // vérifie que le parent existe
        return categoryRepository
            .findByParentIdAndIsActiveTrueOrderByPositionAsc(parentId)
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    // ── Écriture (ADMIN seulement) ────────────────────────────

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category cat = new Category();
        applyRequest(cat, request);
        return CategoryResponse.from(categoryRepository.save(cat));
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest request) {
        Category cat = findOrThrow(id);
        applyRequest(cat, request);
        return CategoryResponse.from(categoryRepository.save(cat));
    }

    @Transactional
    public void delete(Integer id) {
        Category cat = findOrThrow(id);
        cat.setActive(false);                // soft delete
        categoryRepository.save(cat);
    }

    // ── Helpers ──────────────────────────────────────────────

    private void applyRequest(Category cat, CategoryRequest req) {
        cat.setName(req.name());
        cat.setSlug(req.slug());
        cat.setIcon(req.icon());
        cat.setDescription(req.description());
        cat.setPosition(req.position() != null ? req.position() : 0);

        if (req.parentId() != null) {
            Category parent = findOrThrow(req.parentId());
            cat.setParent(parent);
        } else {
            cat.setParent(null);
        }
    }

    private Category findOrThrow(Integer id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : id=" + id));
    }
}
