package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.models.user.User;
import java.time.Instant;
import java.util.Optional;

/**
 * Persistence port for users.
 */
public interface UserPersistencePort {

    /**
     * Save a user to the database.
     *
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Find a user by email along with their authorities.
     *
     * @param email the email of the user
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findWithAuthoritiesByEmail(String email);

    /**
     * Finds a user along with their authorities by identifier.
     *
     * @param id the user identifier
     * @return the matching user, or empty if not found
     */
    Optional<User> findWithAuthoritiesById(Long id);

    /**
     * Find a user by their activation code.
     *
     * @param activationCode the activation code of the user
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByActivationCode(String activationCode);

    /**
     * Find a user by their reset code.
     *
     * @param resetCode the reset code of the user
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByResetCode(String resetCode);

    /**
     * Find a user by their email.
     *
     * @param email the email of the user
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether an activation code already exists.
     *
     * @param activationCode the activation code to check
     * @return true if the code already exists
     */
    boolean existsByActivationCode(String activationCode);

    /**
     * Check whether a reset code already exists.
     *
     * @param resetCode the reset code to check
     * @return true if the code already exists
     */
    boolean existsByResetCode(String resetCode);

    /**
     * Delete inactive users with expired activation codes.
     *
     * @param status   the status of the users to delete
     * @param dateTime the cutoff date and time for expiration
     * @return the number of users deleted
     */
    int deleteInactiveUsersWithExpiredActivationCode(UserStatus status, Instant dateTime);

    /**
     * Delete users matching the given status and modified before the cutoff date.
     *
     * @param status   the status of users to delete
     * @param dateTime the cutoff date and time for deletion
     * @return number of deleted users
     */
    int deleteByStatusAndLastUpdateDateBefore(UserStatus status, Instant dateTime);

    /**
     * Remove an existing user from the database.
     *
     * @param existingUser the user to remove
     */
    void remove(User existingUser);

    /**
     * Find a user by their email-change OTP code.
     *
     * @param code the email-change code
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmailChangeCode(String code);

    /**
     * Check whether an email-change code already exists.
     *
     * @param code the email-change code to check
     * @return true if the code already exists
     */
    boolean existsByEmailChangeCode(String code);

    /**
     * Check whether any user other than the given one already has this email as a pending change target.
     *
     * @param email         the candidate new email address
     * @param excludeUserId the user id to exclude from the check (the requesting user)
     * @return true if another user already has this address pending
     */
    boolean existsPendingEmailForAnotherUser(String email, Long excludeUserId);
}
