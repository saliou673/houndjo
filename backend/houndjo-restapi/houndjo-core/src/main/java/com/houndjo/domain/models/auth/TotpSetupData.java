package com.houndjo.domain.models.auth;

/**
 * Data returned to the user when initiating TOTP setup.
 * The user scans the QR code (or enters the secret manually) into their authenticator app.
 */
public record TotpSetupData(

        /**
         * The Base32-encoded TOTP shared secret to be scanned or entered manually.
         */
        String secret,

        /**
         * The {@code otpauth://totp/...} URI. Can be encoded as a QR code by the frontend.
         */
        String otpAuthUri) {}
