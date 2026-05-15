package com.lebonplan.repository;

import com.lebonplan.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Favorite> findByUserIdAndPostId(UUID userId, UUID postId);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.post.id = :postId")
    void deleteByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);

    long countByPostId(UUID postId);
}
