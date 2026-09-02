package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.auth.TwoFactorChallenge;
import com.houndjo.domain.models.auth.TwoFactorChallengePurpose;
import java.util.Optional;

/**
 * Persistence port for two-factor authentication challenges.
 */
public interface TwoFactorChallengePersistencePort {

    /**
     * Persists or updates a two-factor challenge.
     *
     * @param challenge the challenge to save
     * @return the saved challenge
     */
    TwoFactorChallenge save(TwoFactorChallenge challenge);

    /**
     * Finds a challenge by its identifier.
     *
     * @param id the challenge identifier
     * @return the matching challenge, or empty if not found
     */
    Optional<TwoFactorChallenge> findById(String id);

    /**
     * Finds the active challenge for the given user and purpose.
     *
     * @param userId  the user identifier
     * @param purpose the challenge purpose (LOGIN or SETUP)
     * @return the matching challenge, or empty if not found
     */
    Optional<TwoFactorChallenge> findByUserIdAndPurpose(Long userId, TwoFactorChallengePurpose purpose);

    /**
     * Deletes the challenge with the given identifier.
     *
     * @param id the challenge identifier
     */
    void deleteById(String id);

    /**
     * Deletes all challenges belonging to the given user.
     *
     * @param userId the user identifier
     */
    void deleteByUserId(Long userId);
}
