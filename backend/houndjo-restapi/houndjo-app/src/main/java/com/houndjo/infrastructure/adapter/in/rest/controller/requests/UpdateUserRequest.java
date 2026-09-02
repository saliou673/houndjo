package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.enumerations.UserGender;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Request to update the authenticated user's profile information.
 *
 * @param firstName   new given name
 * @param lastName    new family name
 * @param phoneNumber optional phone number
 * @param birthDate   optional birth date
 * @param gender      optional gender
 * @param address     optional postal address
 * @param languageKey optional preferred locale key
 * @param imageUrl    optional profile picture URL
 */
public record UpdateUserRequest(
        @NotBlank(message = "firstName must not be blank") String firstName,

        @NotBlank(message = "lastName must not be blank") String lastName,

        @Nullable String phoneNumber,

        @Nullable LocalDate birthDate,

        @Nullable UserGender gender,

        @Nullable String address,

        @Nullable String languageKey,

        @Nullable String imageUrl) {}
