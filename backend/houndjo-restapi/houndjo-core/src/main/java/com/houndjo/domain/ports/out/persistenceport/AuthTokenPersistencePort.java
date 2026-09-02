package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.auth.AuthToken;
import com.houndjo.domain.models.user.User;
import java.util.Optional;

/**
 * Persistence port for authentication tokens (access + refresh token pairs).
 */
public interface AuthTokenPersistencePort {

    /**
     * Finds a token by its refresh token value.
     *
     * @param refreshToken the refresh token value
     * @return the matching token, or empty if not found
     */
    Optional<com.houndjo.domain.models.auth.AuthToken> findByRefreshToken(String refreshToken);

    /**
     * Finds a token by its access token value.
     *
     * @param accessToken the access token value
     * @return the matching token, or empty if not found
     */
    Optional<AuthToken> findByAccessToken(String accessToken);

    /**
     * Persists or updates an authentication token.
     *
     * @param authToken the token to save
     */
    void save(AuthToken authToken);

    /**
     * Removes all tokens belonging to the given user.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteAllForUser(User user);

    /**
     * Removes the token identified by the given access token value.
     *
     * @param accessToken the access token value
     */
    void deleteByAccessToken(String accessToken);
}
