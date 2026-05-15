package com.lebonplan.controller;

import com.lebonplan.dto.response.FavoriteResponse;
import com.lebonplan.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * GET /api/favorites
     * Mes favoris — AUTH requis
     */
    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getMyFavorites(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                favoriteService.getMyFavorites(userDetails.getUsername())
        );
    }
    /**
     * POST /api/favorites/{postId}/toggle
     * Ajouter/Retirer un favori — AUTH requis
     */
    @PostMapping("/{postId}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentification requise"));
        }
        return ResponseEntity.ok(favoriteService.toggle(postId, userDetails.getUsername()));
    }

    /**
     * GET /api/favorites/{postId}/check
     * Vérifier si favori — optionnel (retourne false si pas authentifié)
     */
    @GetMapping("/{postId}/check")
    public ResponseEntity<Map<String, Boolean>> check(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("favorited", false));
        }
        boolean favorited = favoriteService.isFavorited(postId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }

    /**
     * GET /api/favorites/{postId}/count
     * Nombre de favoris — PUBLIC
     */
    @GetMapping("/{postId}/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable UUID postId) {
        long count = favoriteService.countByPost(postId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}