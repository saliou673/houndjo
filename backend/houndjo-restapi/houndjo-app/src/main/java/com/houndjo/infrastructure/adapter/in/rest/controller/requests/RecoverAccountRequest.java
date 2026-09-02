package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import com.houndjo.domain.constants.DomainConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request to initiate an account recovery (password reset) flow.
 *
 * @param email    the email address associated with the account
 * @param password the new password to set (used in some recovery flows)
 */
public record RecoverAccountRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = DomainConstants.EMAIL_REGEX_PATTERN, message = "Invalid email")
        String email,

        @NotBlank(message = "Password is required") String password) {}
