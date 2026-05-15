package com.lebonplan.dto.request;

public record PostSearchRequest(
    String search,
    String city,
    Integer categoryId,
    String sortBy,     // createdAt | viewsCount | eventDate
    int page,
    int size
) {
    public PostSearchRequest {
        if (page < 0)       page = 0;
        if (size < 1)       size = 12;
        if (size > 50)      size = 50;
        if (sortBy == null) sortBy = "createdAt";
    }
}
