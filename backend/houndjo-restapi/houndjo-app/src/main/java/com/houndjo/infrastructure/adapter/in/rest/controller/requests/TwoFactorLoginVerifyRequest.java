package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to verify a 2FA login challenge and complete the login flow.
 *
 * @param challengeId the identifier of the pending 2FA challenge
 * @param code        the one-time code provided by the user
 */
public record TwoFactorLoginVerifyRequest(
        @NotBlank String challengeId, @NotBlank String code) {}
