package com.lebonplan.controller;

import com.lebonplan.dto.response.StatsResponse;
import com.lebonplan.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Statistics Controller
 * Provides global platform statistics (public endpoint)
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatsController {

    private final StatsService statsService;

    /**
     * GET /api/stats
     * 
     * Récupère toutes les statistiques globales de la plateforme
     * 
     * @return StatsResponse contenant:
     *         - totalPosts: nombre d'annonces
     *         - totalUsers: nombre d'utilisateurs
     *         - totalComments: nombre de commentaires
     *         - totalFavorites: nombre de favoris
     *         - postsLast24h: posts créés dernières 24h
     *         - usersLast7days: utilisateurs derniers 7j
     *         - commentsLast24h: commentaires dernières 24h
     *         - topCities: top 10 villes
     *         - topCategories: top 6 catégories
     *         - dailyActivity: activité derniers 7j
     *         - trendingPosts: top 5 posts trending
     * 
     * @apiNote PUBLIC - pas d'authentification requise
     * @apiNote Cachable - peut être mis en cache 5 minutes
     */
    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = statsService.getStats();
        return ResponseEntity.ok()
            .cacheControl(org.springframework.http.CacheControl.maxAge(5, java.util.concurrent.TimeUnit.MINUTES))
            .body(stats);
    }
}