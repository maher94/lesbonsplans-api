package com.lebonplan.dto.response;


import java.util.UUID;

import com.lebonplan.entity.Image;

public record ImageResponse(
    UUID id,
    String url,
    String publicId,
    Integer position
) {
    public static ImageResponse from(Image img) {
        return new ImageResponse(img.getId(), img.getUrl(), img.getPublicId(), img.getPosition());
    }
}
