package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.constants.DomainConstants;
import com.houndjo.domain.enumerations.UserGender;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;

/**
 * The request to create a new user from admin endpoints.
 */
public record CreateAdminUserRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = DomainConstants.EMAIL_REGEX_PATTERN, message = "Invalid email")
        String email,

        @NotBlank(message = "firstName must not be blank") String firstName,

        @NotBlank(message = "lastName must not be blank") String lastName,

        @Nullable LocalDate birthDate,

        @Nullable UserGender gender,

        @Nullable String phoneNumber,

        @Nullable String address,

        @Nullable String languageKey,

        @Nullable String imageUrl,

        @NotEmpty(message = "roleGroupNames must not be empty")
        Set<@NotBlank(message = "role group name must not be blank") String> roleGroupNames) {}
