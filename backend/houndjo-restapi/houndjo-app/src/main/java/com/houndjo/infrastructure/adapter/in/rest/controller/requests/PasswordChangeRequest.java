package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to change the authenticated user's password.
 *
 * @param currentPassword the user's current password for verification
 * @param newPassword     the new password to set
 */
public record PasswordChangeRequest(
        @NotBlank(message = "current password is required") String currentPassword,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = Constants.PASSWORD_REGEX_PATTERN, message = "Invalid password")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword) {}
