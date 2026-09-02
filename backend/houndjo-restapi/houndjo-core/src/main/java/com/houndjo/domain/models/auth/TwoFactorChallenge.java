package com.houndjo.domain.models.auth;

import com.houndjo.domain.models.user.User;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Represents a pending two-factor authentication challenge.
 * Used for both login (PURPOSE=LOGIN) and 2FA setup verification (PURPOSE=SETUP).
 */
@Getter
public class TwoFactorChallenge {

    /**
     * Unique UUID string identifier.
     */
    private final String id;
    /**
     * The user this challenge was issued for.
     */
    private final User user;
    /**
     * The OTP code (or TOTP secret for setup challenges).
     */
    private final String code;
    /**
     * The 2FA method used to deliver or verify the code.
     */
    private final TwoFactorMethodType type;
    /**
     * Whether this challenge completes a login or a 2FA setup.
     */
    private final TwoFactorChallengePurpose purpose;
    /**
     * Whether a long-lived session should be created upon verification.
     */
    private final boolean rememberMe;
    /**
     * Timestamp after which this challenge is no longer valid.
     */
    private final Instant expiryDate;
    /**
     * Timestamp when this challenge was created.
     */
    private final Instant creationDate;

    private TwoFactorChallenge(
            String id,
            User user,
            String code,
            TwoFactorMethodType type,
            TwoFactorChallengePurpose purpose,
            boolean rememberMe,
            Instant expiryDate,
            Instant creationDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.purpose = Objects.requireNonNull(purpose, "purpose must not be null");
        this.rememberMe = rememberMe;
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate must not be null");
        this.creationDate = creationDate != null ? creationDate : Instant.now();
    }

    public static TwoFactorChallenge create(
            String id,
            User user,
            String code,
            TwoFactorMethodType type,
            TwoFactorChallengePurpose purpose,
            boolean rememberMe,
            Instant expiryDate) {
        return new TwoFactorChallenge(id, user, code, type, purpose, rememberMe, expiryDate, Instant.now());
    }

    public static TwoFactorChallenge rehydrate(
            String id,
            User user,
            String code,
            TwoFactorMethodType type,
            TwoFactorChallengePurpose purpose,
            boolean rememberMe,
            Instant expiryDate,
            Instant creationDate) {
        return new TwoFactorChallenge(id, user, code, type, purpose, rememberMe, expiryDate, creationDate);
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}
