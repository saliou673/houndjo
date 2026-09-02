package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to disable two-factor authentication, requiring password confirmation.
 *
 * @param currentPassword the user's current password for confirmation
 */
public record TwoFactorDisableRequest(@NotBlank String currentPassword) {}
