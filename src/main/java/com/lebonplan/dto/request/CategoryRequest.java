package com.lebonplan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    String name,

    @NotBlank(message = "Le slug est obligatoire")
    @Size(max = 100, message = "Le slug ne doit pas dépasser 100 caractères")
    String slug,

    String icon,

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    String description,

    Integer parentId,

    Integer position
) {}
