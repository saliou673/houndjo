package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to complete a password-reset flow using a one-time reset code.
 *
 * @param code        the one-time reset code sent to the user
 * @param newPassword the new password to set
 */
public record PasswordResetRequest(
        @NotBlank(message = "Reset code is required") String code,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = Constants.PASSWORD_REGEX_PATTERN, message = "Invalid password")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword) {}
