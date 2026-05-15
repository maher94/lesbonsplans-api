package com.lebonplan.dto.response;

import com.lebonplan.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String username,
    String avatarUrl,
    String bio,
    String city,
    String role,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getAvatarUrl(),
            user.getBio(),
            user.getCity(),
            user.getRole().name(),
            user.getCreatedAt()
        );
    }
}
