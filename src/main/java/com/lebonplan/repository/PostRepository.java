package com.lebonplan.repository;

import com.lebonplan.entity.Category;
import com.lebonplan.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    // ── Recherche principale ──────────────────────────────────

	@Query("""
		    SELECT p FROM Post p
		    WHERE p.status = 'ACTIVE'
		      AND (
		            :search IS NULL
		            OR LOWER(p.title) LIKE :search
		            OR LOWER(p.description) LIKE :search
		          )
		      AND (
		            :city IS NULL
		            OR LOWER(p.city) LIKE :city
		          )
		      AND (
		            :categoryId IS NULL
		            OR p.category.id = :categoryId
		          )
		    """)
		Page<Post> search(
		    @Param("search") String search,
		    @Param("city") String city,
		    @Param("categoryId") Integer categoryId,
		    Pageable pageable
		);
    // ── Posts d'un utilisateur ────────────────────────────────

    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.category
        WHERE p.user.id = :userId
          AND p.status != 'DELETED'
        ORDER BY p.createdAt DESC
        """)
    List<Post> findByUserId(@Param("userId") UUID userId);

    // ── Posts mis en avant ────────────────────────────────────

    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.user
        JOIN FETCH p.category
        WHERE p.status = 'ACTIVE'
          AND p.isFeatured = true
        ORDER BY p.createdAt DESC
        """)
    List<Post> findFeatured(Pageable pageable);

    // ── Incrémenter les vues (UPDATE direct, sans charger l'entité) ──

    @Modifying
    @Query("UPDATE Post p SET p.viewsCount = p.viewsCount + 1 WHERE p.id = :id")
    void incrementViews(@Param("id") UUID id);

    // ── Posts proches géographiquement (distance en km) ──────

    @Query(value = """
        SELECT p.* FROM posts p
        WHERE p.status = 'ACTIVE'
          AND p.latitude  IS NOT NULL
          AND p.longitude IS NOT NULL
          AND (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(p.latitude))
              * cos(radians(p.longitude) - radians(:lng))
              + sin(radians(:lat)) * sin(radians(p.latitude))
            )
          ) <= :radiusKm
        ORDER BY p.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> findNearby(
        @Param("lat")      double lat,
        @Param("lng")      double lng,
        @Param("radiusKm") double radiusKm,
        @Param("limit")    int limit
    );
    List<Post> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByCategory(Category category);

}
