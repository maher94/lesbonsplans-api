package com.lebonplan.service;

import com.lebonplan.dto.response.StatsResponse;
import com.lebonplan.dto.response.StatsResponse.*;
import com.lebonplan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        // Global stats
        long totalPosts = postRepository.count();
        long totalUsers = userRepository.count();
        long totalComments = commentRepository.count();
        long totalFavorites = favoriteRepository.count();

        // Time-based stats
        long postsLast24h = getPostsLast24h();
        long usersLast7days = getUsersLast7days();
        long commentsLast24h = getCommentsLast24h();

        // Top cities
        List<CityStatsDto> topCities = getTopCities();

        // Top categories
        List<CategoryStatsDto> topCategories = getTopCategories();

        // Daily activity (last 7 days)
        List<DailyActivityDto> dailyActivity = getDailyActivity();

        // Trending posts
        List<TrendingPostDto> trendingPosts = getTrendingPosts();

        return new StatsResponse(
            totalPosts,
            totalUsers,
            totalComments,
            totalFavorites,
            postsLast24h,
            usersLast7days,
            commentsLast24h,
            topCities,
            topCategories,
            dailyActivity,
            trendingPosts
        );
    }

    // ============================================================
    // Time-based Queries
    // ============================================================

    private long getPostsLast24h() {
        try {
            Instant yesterday = Instant.now().minus(24, ChronoUnit.HOURS);
            String sql = "SELECT COUNT(*) FROM posts WHERE created_at >= ?";
            return jdbcTemplate.queryForObject(sql, Long.class, yesterday);
        } catch (Exception e) {
            return 0;
        }
    }

    private long getUsersLast7days() {
        try {
            Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
            String sql = "SELECT COUNT(*) FROM users WHERE created_at >= ?";
            return jdbcTemplate.queryForObject(sql, Long.class, sevenDaysAgo);
        } catch (Exception e) {
            return 0;
        }
    }

    private long getCommentsLast24h() {
        try {
            Instant yesterday = Instant.now().minus(24, ChronoUnit.HOURS);
            String sql = "SELECT COUNT(*) FROM comments WHERE created_at >= ? AND is_deleted = false";
            return jdbcTemplate.queryForObject(sql, Long.class, yesterday);
        } catch (Exception e) {
            return 0;
        }
    }

    // ============================================================
    // Top Cities (Native Query)
    // ============================================================

    private List<CityStatsDto> getTopCities() {
        try {
            String sql = "SELECT city, COUNT(*) as count FROM posts WHERE status = 'ACTIVE' AND city IS NOT NULL GROUP BY city ORDER BY count DESC LIMIT 10";
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            long totalPosts = postRepository.count();

            return rows.stream()
                .map(row -> {
                    String city = (String) row.get("city");
                    long count = ((Number) row.get("count")).longValue();
                    double percentage = totalPosts > 0 ? (count * 100.0) / totalPosts : 0;
                    return new CityStatsDto(city, count, percentage);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // ============================================================
    // Top Categories
    // ============================================================

    private List<CategoryStatsDto> getTopCategories() {
        try {
            long totalPosts = postRepository.count();

            return categoryRepository.findAll().stream()
                .filter(cat -> cat.getParent() == null)
                .map(cat -> {
                    long count = postRepository.countByCategory(cat);
                    double percentage = totalPosts > 0 ? (count * 100.0) / totalPosts : 0;
                    return new CategoryStatsDto(
                        cat.getId(),
                        cat.getName(),
                        cat.getIcon(),
                        count,
                        percentage
                    );
                })
                .filter(cat -> cat.count() > 0)
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .limit(6)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // ============================================================
    // Daily Activity (Last 7 days)
    // ============================================================

    private List<DailyActivityDto> getDailyActivity() {
        try {
            String sql = """
                WITH dates AS (
                    SELECT CURRENT_DATE - INTERVAL '6 days' + INTERVAL '1 day' * s.n as date
                    FROM generate_series(0, 6) s(n)
                )
                SELECT 
                    CAST(d.date AS VARCHAR) as date,
                    COALESCE((SELECT COUNT(*) FROM posts WHERE CAST(created_at AS DATE) = d.date), 0) as posts,
                    COALESCE((SELECT COUNT(*) FROM comments WHERE CAST(created_at AS DATE) = d.date AND is_deleted = false), 0) as comments,
                    COALESCE((SELECT COUNT(*) FROM users WHERE CAST(created_at AS DATE) = d.date), 0) as users
                FROM dates d
                ORDER BY d.date
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            return rows.stream()
                .map(row -> new DailyActivityDto(
                    (String) row.get("date"),
                    ((Number) row.get("posts")).longValue(),
                    ((Number) row.get("comments")).longValue(),
                    ((Number) row.get("users")).longValue()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // ============================================================
    // Trending Posts (Most viewed in last 7 days)
    // ============================================================

    private List<TrendingPostDto> getTrendingPosts() {
        try {
            Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
            String sql = """
                SELECT 
                    p.id,
                    p.title,
                    COALESCE(p.views_count, 0) as views,
                    (SELECT COUNT(*) FROM comments WHERE post_id = p.id AND is_deleted = false) as comments,
                    (SELECT COUNT(*) FROM favorites WHERE post_id = p.id) as favorites,
                    p.city,
                    p.created_at
                FROM posts p
                WHERE p.status = 'ACTIVE' AND p.created_at >= ?
                ORDER BY p.views_count DESC NULLS LAST
                LIMIT 5
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sevenDaysAgo);
            return rows.stream()
                .map(row -> new TrendingPostDto(
                    row.get("id").toString(),
                    (String) row.get("title"),
                    ((Number) row.get("views")).longValue(),
                    ((Number) row.get("comments")).longValue(),
                    ((Number) row.get("favorites")).longValue(),
                    (String) row.get("city"),
                    (Instant) row.get("created_at")
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}