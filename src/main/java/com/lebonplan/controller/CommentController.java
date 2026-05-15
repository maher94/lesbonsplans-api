package com.lebonplan.controller;

import com.lebonplan.dto.request.CommentRequest;
import com.lebonplan.dto.response.CommentResponse;
import com.lebonplan.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * GET /api/posts/{postId}/comments
     * Tous les commentaires d'une annonce
     * PUBLIC
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getByPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getByPost(postId));
    }

    /**
     * POST /api/posts/{postId}/comments
     * Ajouter un commentaire
     * AUTH requis
     */
    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(postId, request, userDetails.getUsername()));
    }

    /**
     * PUT /api/posts/{postId}/comments/{id}
     * Modifier un commentaire
     * AUTH requis
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable UUID postId,
            @PathVariable UUID id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(commentService.update(id, request, userDetails.getUsername()));
    }

    /**
     * DELETE /api/posts/{postId}/comments/{id}
     * Supprimer un commentaire (soft delete)
     * AUTH requis
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID postId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        commentService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}