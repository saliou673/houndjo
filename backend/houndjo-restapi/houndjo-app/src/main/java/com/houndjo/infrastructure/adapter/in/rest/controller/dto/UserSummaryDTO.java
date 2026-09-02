package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import static com.houndjo.domain.constants.DomainConstants.EMAIL_REGEX_PATTERN;

import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.enumerations.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Represents a user, with his resolved permissions.
 */
@Schema(name = "UserSummary")
public record UserSummaryDTO(
        Long id,

        @Pattern(regexp = EMAIL_REGEX_PATTERN) @NotBlank String email,

        @Nullable String phoneNumber,

        @NotBlank String firstName,

        @NotBlank String lastName,

        @NotNull LocalDate birthDate,

        @NotNull UserGender gender,

        @Nullable String address,

        @Nullable UserStatus status,

        String languageKey,

        @Nullable String imageUrl,

        @NotNull UserPreferencesDTO preferences,

        boolean twoFactorEnabled) {}
