package com.lebonplan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lebonplan.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Toutes les catégories actives (parents + enfants)
    List<Category> findByIsActiveTrueOrderByPositionAsc();

    // Catégories parentes uniquement (sans parent)
    List<Category> findByParentIsNullAndIsActiveTrueOrderByPositionAsc();

    // Sous-catégories d'un parent
    List<Category> findByParentIdAndIsActiveTrueOrderByPositionAsc(Integer parentId);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
