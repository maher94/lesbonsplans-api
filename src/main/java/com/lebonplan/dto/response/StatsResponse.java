package com.lebonplan.dto.response;

import java.time.Instant;
import java.util.List;

public record StatsResponse(
    // ============================================================
    // Global Stats
    // ============================================================
    long totalPosts,
    long totalUsers,
    long totalComments,
    long totalFavorites,
    
    // ============================================================
    // Time-based Stats
    // ============================================================
    long postsLast24h,
    long usersLast7days,
    long commentsLast24h,
    
    // ============================================================
    // Location Stats
    // ============================================================
    List<CityStatsDto> topCities,
    
    // ============================================================
    // Category Stats
    // ============================================================
    List<CategoryStatsDto> topCategories,
    
    // ============================================================
    // Activity Stats
    // ============================================================
    List<DailyActivityDto> dailyActivity,
    
    // ============================================================
    // Trending
    // ============================================================
    List<TrendingPostDto> trendingPosts
) {

    // ============================================================
    // City Stats DTO
    // ============================================================
    public record CityStatsDto(
        String city,
        long count,
        double percentage
    ) {}

    // ============================================================
    // Category Stats DTO
    // ============================================================
    public record CategoryStatsDto(
        Integer categoryId,
        String categoryName,
        String icon,
        long count,
        double percentage
    ) {}

    // ============================================================
    // Daily Activity DTO
    // ============================================================
    public record DailyActivityDto(
        String date,
        long posts,
        long comments,
        long users
    ) {}

    // ============================================================
    // Trending Posts DTO
    // ============================================================
    public record TrendingPostDto(
        String id,
        String title,
        long views,
        long comments,
        long favorites,
        String city,
        Instant createdAt
    ) {}
}