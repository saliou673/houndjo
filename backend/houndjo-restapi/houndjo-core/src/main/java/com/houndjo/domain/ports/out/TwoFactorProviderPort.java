package com.houndjo.domain.ports.out;

import com.houndjo.domain.models.auth.TotpSetupData;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.user.User;
import java.util.Optional;

/**
 * Outbound port for a specific two-factor authentication method.
 * Each method (email, SMS, TOTP) provides its own implementation.
 */
public interface TwoFactorProviderPort {

    /**
     * Returns the 2FA method type this provider handles.
     */
    TwoFactorMethodType getType();

    /**
     * Generates and delivers a one-time code to the user.
     * For code-based methods (email, SMS): generates and sends the code, returns it for storage.
     * For TOTP: generates a new secret and returns it (to be stored in the pending challenge);
     * no message is sent — the secret is returned via {@link #buildSetupData}.
     *
     * @param user The user to send the code to.
     * @return The generated code (to be stored in the challenge), or the TOTP secret for TOTP.
     */
    String generateAndSendCode(User user);

    /**
     * Verifies that the user-provided code matches the expected value.
     *
     * @param user         The user (provides TOTP secret if needed).
     * @param storedCode   The code stored in the challenge (unused for TOTP login).
     * @param providedCode The code submitted by the user.
     * @return true if the code is valid.
     */
    boolean verify(User user, String storedCode, String providedCode);

    /**
     * Returns setup data to present to the user after initiating 2FA setup.
     * For email/SMS: returns empty (code was already sent).
     * For TOTP: returns the secret and OTP auth URI for the QR code.
     *
     * @param user          The user initiating setup.
     * @param pendingSecret The value returned by {@link #generateAndSendCode}.
     * @return TOTP setup data, or empty for non-TOTP methods.
     */
    default Optional<TotpSetupData> buildSetupData(User user, String pendingSecret) {
        return Optional.empty();
    }
}
