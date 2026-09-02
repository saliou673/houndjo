package com.houndjo.domain.models.auth;

import com.houndjo.domain.exceptions.InvalidRefreshTokenExpiryDateException;
import com.houndjo.domain.exceptions.InvalidRefreshTokenTokenException;
import com.houndjo.domain.exceptions.InvalidRefreshTokenUserException;
import com.houndjo.domain.models.user.User;
import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * Domain aggregate representing a persisted authentication token pair (access + refresh).
 */
@Getter
public class AuthToken {
    /**
     * Surrogate database identifier ({@code null} for new tokens).
     */
    private final Long id;
    /**
     * Short-lived JWT access token.
     */
    private String accessToken;
    /**
     * Long-lived opaque refresh token.
     */
    private final String refreshToken;
    /**
     * Whether a long-lived session was requested at login time.
     */
    private final boolean rememberMe;
    /**
     * Expiry date of the refresh token.
     */
    private Instant expiryDate;
    /**
     * The user this token belongs to.
     */
    private final User user;
    /**
     * Timestamp when this token pair was created.
     */
    private final Instant creationDate;

    private AuthToken(
            Long id,
            String accessToken,
            String refreshToken,
            Boolean rememberMe,
            Instant expiryDate,
            User user,
            Instant creationDate) {
        if (StringUtils.isBlank(accessToken)) {
            throw new InvalidRefreshTokenTokenException();
        }
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidRefreshTokenTokenException();
        }
        if (expiryDate == null) {
            throw new InvalidRefreshTokenExpiryDateException();
        }
        if (user == null) {
            throw new InvalidRefreshTokenUserException();
        }

        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.rememberMe = rememberMe != null && rememberMe;
        this.expiryDate = expiryDate;
        this.user = user;
        this.creationDate = creationDate;
    }

    public static AuthToken create(
            String accessToken, String refreshToken, boolean rememberMe, Instant expiryDate, User user) {
        return new AuthToken(null, accessToken, refreshToken, rememberMe, expiryDate, user, Instant.now());
    }

    public static AuthToken rehydrate(
            Long id,
            String accessToken,
            String refreshToken,
            boolean rememberMe,
            Instant expiryDate,
            User user,
            Instant creationDate) {
        return new AuthToken(id, accessToken, refreshToken, rememberMe, expiryDate, user, creationDate);
    }

    private boolean isExpired(Instant now) {
        return expiryDate.isBefore(now);
    }

    public boolean isValid(@NonNull Instant now) {
        return !isExpired(now);
    }

    public boolean getRememberMe() {
        return rememberMe;
    }

    public void updateExpiryDate(@NonNull Instant newExpiryDate) {
        if (newExpiryDate.isBefore(Instant.now())) {
            throw new InvalidRefreshTokenExpiryDateException();
        }
        this.expiryDate = newExpiryDate;
    }

    public void updateAccessToken(String newAccessToken) {
        if (StringUtils.isBlank(newAccessToken)) {
            throw new InvalidRefreshTokenTokenException();
        }
        this.accessToken = newAccessToken;
    }
}
