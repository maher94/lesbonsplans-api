package com.lebonplan.dto.response;

import com.lebonplan.entity.Post;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record PostResponse(
    UUID id,
    String title,
    String description,
    CategoryResponse category,
    UserResponse user,
    String address,
    String city,
    Double latitude,
    Double longitude,
    LocalDate eventDate,
    LocalTime eventTime,
    BigDecimal price,
    String priceLabel,
    String sourceUrl,
    String status,
    boolean isFeatured,
    int viewsCount,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt,
    List<ImageResponse> images
) {
    // Conversion complète (page détail)
    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getTitle(),
            post.getDescription(),
            CategoryResponse.from(post.getCategory()),
            UserResponse.from(post.getUser()),
            post.getAddress(),
            post.getCity(),
            post.getLatitude(),
            post.getLongitude(),
            post.getEventDate(),
            post.getEventTime(),
            post.getPrice(),
            post.getPriceLabel(),
            post.getSourceUrl(),
            post.getStatus().name(),
            post.isFeatured(),
            post.getViewsCount(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getExpiresAt(),
            post.getImages().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(ImageResponse::from)
                .toList()
        );
    }

    // Version résumée pour les listes (sans description complète)
    public static PostResponse summary(Post post) {
        String shortDesc = post.getDescription().length() > 150
            ? post.getDescription().substring(0, 150) + "…"
            : post.getDescription();

        return new PostResponse(
            post.getId(),
            post.getTitle(),
            shortDesc,
            CategoryResponse.from(post.getCategory()),
            UserResponse.from(post.getUser()),
            post.getAddress(),
            post.getCity(),
            post.getLatitude(),
            post.getLongitude(),
            post.getEventDate(),
            post.getEventTime(),
            post.getPrice(),
            post.getPriceLabel(),
            post.getSourceUrl(),
            post.getStatus().name(),
            post.isFeatured(),
            post.getViewsCount(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getExpiresAt(),
            post.getImages().stream()
                .filter(i -> i.getPosition() == 0)   // uniquement la 1ère image
                .map(ImageResponse::from)
                .toList()
        );
    }
}
