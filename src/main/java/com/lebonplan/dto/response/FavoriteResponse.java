package com.lebonplan.dto.response;

import com.lebonplan.entity.Favorite;

import java.time.Instant;
import java.util.UUID;

public record FavoriteResponse(
    UUID id,
    PostResponse post,
    Instant createdAt
) {
    public static FavoriteResponse from(Favorite fav) {
        return new FavoriteResponse(
            fav.getId(),
            PostResponse.summary(fav.getPost()),
            fav.getCreatedAt()
        );
    }
}