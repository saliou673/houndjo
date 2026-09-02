package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

/**
 * Response body returned when initiating TOTP 2FA setup.
 * The frontend should encode {@code otpAuthUri} as a QR code for the user to scan.
 */
public record TotpSetupResponse(

        /**
         * The Base32-encoded TOTP shared secret (for manual entry in the authenticator app).
         */
        String secret,

        /**
         * The full {@code otpauth://totp/...} URI ready to be encoded as a QR code.
         */
        String otpAuthUri) {}
