package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to complete an admin-created user invitation by setting the initial password.
 *
 * @param code        the one-time invitation code
 * @param newPassword the initial password chosen by the user
 */
public record InvitationCompleteRequest(
        @NotBlank(message = "Invitation code is required") String code,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = Constants.PASSWORD_REGEX_PATTERN, message = "Invalid password")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword) {}
