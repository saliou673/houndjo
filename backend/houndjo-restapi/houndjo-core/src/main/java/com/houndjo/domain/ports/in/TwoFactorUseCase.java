package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.auth.JwtToken;
import com.houndjo.domain.models.auth.TotpSetupData;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import java.util.Optional;

/**
 * Use case for two-factor authentication management.
 */
public interface TwoFactorUseCase {

    /**
     * Initiates 2FA setup for the current authenticated user.
     * For email/SMS: sends a verification code to the user; returns empty.
     * For TOTP: generates a secret and QR code URI; returns them for the user to scan.
     *
     * @param type The 2FA method to set up.
     * @return TOTP setup data (secret + OTP auth URI) if type is TOTP, otherwise empty.
     */
    Optional<TotpSetupData> initSetup(TwoFactorMethodType type);

    /**
     * Confirms 2FA setup by verifying the code sent during initSetup.
     * On success, 2FA is enabled for the current user.
     *
     * @param code The verification code received by the user.
     */
    void confirmSetup(String code);

    /**
     * Disables 2FA for the current authenticated user.
     * Requires the current password for confirmation.
     *
     * @param currentPassword The user's current password.
     */
    void disable(String currentPassword);

    /**
     * Verifies a login 2FA challenge and completes authentication.
     *
     * @param challengeId The ID of the pending login challenge.
     * @param code        The OTP code provided by the user.
     * @return JWT tokens upon successful verification.
     */
    JwtToken verifyLoginChallenge(String challengeId, String code);
}
