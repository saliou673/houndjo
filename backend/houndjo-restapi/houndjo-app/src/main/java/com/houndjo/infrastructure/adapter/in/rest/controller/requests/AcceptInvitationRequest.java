package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to accept an organization invitation.
 * <p>
 * {@code password} is required only when the invited email has no existing account: the
 * service creates one and rejects a blank password at that point. An already-registered user
 * accepting an invitation to a new organization may omit it.
 */
public record AcceptInvitationRequest(
        @NotBlank String code,

        @Pattern(regexp = Constants.PASSWORD_REGEX_PATTERN, message = "Invalid password")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password) {}
