package com.lebonplan.dto.response;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String username,
        String avatarUrl
) {}