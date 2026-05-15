package com.lebonplan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record PostRequest(

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    String title,

    @NotBlank(message = "La description est obligatoire")
    String description,

    @NotNull(message = "La catégorie est obligatoire")
    Integer categoryId,

    String address,
    String city,
    Double latitude,
    Double longitude,

    LocalDate eventDate,
    LocalTime eventTime,

    BigDecimal price,
    String priceLabel,

    @Size(max = 500, message = "L'URL ne doit pas dépasser 500 caractères")
    String sourceUrl,

    // Expire dans X jours (optionnel, null = pas d'expiration)
    Integer expireInDays
) {}