package com.houndjo.infrastructure.adapter.in.rest.controller.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to confirm an email-change flow using a one-time OTP code.
 *
 * @param code the OTP code sent to the new email address
 */
public record EmailChangeConfirmRequest(
        @NotBlank(message = "Email change code is required")
        @Size(min = 4, max = 4, message = "Email change code must be exactly 4 characters")
        String code) {}
