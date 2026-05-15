package com.lebonplan.repository;

import com.lebonplan.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {

    List<Image> findByPostIdOrderByPositionAsc(UUID postId);

    @Modifying
    @Query("DELETE FROM Image i WHERE i.post.id = :postId")
    void deleteByPostId(@Param("postId") UUID postId);

    long countByPostId(UUID postId);
}
