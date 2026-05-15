package com.lebonplan.dto.response;

import com.lebonplan.entity.Comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        Instant createdAt,
        Instant updateAt,
        UserSummary user
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                new UserSummary(
                        comment.getUser().getId(),
                        comment.getUser().getUsername(),
                        comment.getUser().getAvatarUrl()
                )
        );
    }
}