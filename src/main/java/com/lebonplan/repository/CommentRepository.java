package com.lebonplan.repository;

import com.lebonplan.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.user u
        WHERE c.post.id = :postId
          AND c.isDeleted = false
        ORDER BY c.createdAt DESC
    """)
    List<Comment> findByPostIdNotDeleted(@Param("postId") UUID postId);

}