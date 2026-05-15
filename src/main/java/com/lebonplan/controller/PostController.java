package com.lebonplan.controller;

import com.lebonplan.dto.request.PostRequest;
import com.lebonplan.dto.request.PostSearchRequest;
import com.lebonplan.dto.response.ImageResponse;
import com.lebonplan.dto.response.PageResponse;
import com.lebonplan.dto.response.PostResponse;
import com.lebonplan.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * GET /api/posts
     * Recherche paginée avec filtres : search, city, categoryId, sortBy, page, size
     * PUBLIC
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        PostSearchRequest req = new PostSearchRequest(search, city, categoryId, sortBy, page, size);
        return ResponseEntity.ok(postService.search(req));
    }

    /**
     * GET /api/posts/featured
     * Annonces mises en avant (homepage)
     * PUBLIC
     */
    @GetMapping("/featured")
    public ResponseEntity<List<PostResponse>> getFeatured(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(postService.getFeatured(limit));
    }

    /**
     * GET /api/posts/nearby
     * Annonces proches géographiquement
     * PUBLIC
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<PostResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "20")  double radiusKm,
            @RequestParam(defaultValue = "12")  int limit
    ) {
        return ResponseEntity.ok(postService.getNearby(lat, lng, radiusKm, limit));
    }

    /**
     * GET /api/posts/{id}
     * Détail d'une annonce (incrémente les vues)
     * PUBLIC
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    /**
     * POST /api/posts
     * Créer une annonce
     * AUTH requis
     */
    @PostMapping
    public ResponseEntity<PostResponse> create(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.create(request, userDetails.getUsername()));
    }

    /**
     * PUT /api/posts/{id}
     * Modifier une annonce (propriétaire ou admin)
     * AUTH requis
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.update(id, request, userDetails.getUsername()));
    }

    /**
     * DELETE /api/posts/{id}
     * Supprimer une annonce (soft delete)
     * AUTH requis
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/posts/{id}/images
     * Ajouter une image à une annonce
     * AUTH requis
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<ImageResponse> addImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.addImage(id, file, userDetails.getUsername()));
    }

    /**
     * DELETE /api/posts/{postId}/images/{imageId}
     * Supprimer une image
     * AUTH requis
     */
    @DeleteMapping("/{postId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID postId,
            @PathVariable UUID imageId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deleteImage(postId, imageId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me")
    public ResponseEntity<List<PostResponse>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                postService.getMyPosts(userDetails.getUsername())
        );
    }
}