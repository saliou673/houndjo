package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.constants.DomainConstants;
import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * The request to create a new user.
 */
@Schema(name = "CreateUserRequest")
public record CreateUserRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = DomainConstants.EMAIL_REGEX_PATTERN, message = "Invalid email")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = Constants.PASSWORD_REGEX_PATTERN, message = "Invalid password")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "firstName must not be blank") String firstName,

        @NotBlank(message = "lastName must not be blank") String lastName,

        @Nullable LocalDate birthDate,

        @Nullable UserGender gender,

        @Nullable String phoneNumber,

        @Nullable String address,

        @Nullable String languageKey,

        @Nullable String imageUrl) {}
