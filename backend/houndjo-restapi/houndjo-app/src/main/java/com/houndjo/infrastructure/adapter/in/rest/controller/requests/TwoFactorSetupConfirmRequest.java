package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to confirm a 2FA setup by providing the generated code.
 *
 * @param code the code received via the chosen 2FA method
 */
public record TwoFactorSetupConfirmRequest(@NotBlank String code) {}
