package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserInfoUpdate;
import java.util.Set;

/**
 * Use case for user account lifecycle management.
 */
public interface AccountUseCase {

    /**
     * Registers a new public (self-signed-up) user.
     *
     * @param user the user to create
     */
    void createPublicUser(User user);

    /**
     * Creates a user and assigns the specified role groups.
     *
     * @param user           the user to create
     * @param roleGroupNames names of role groups to assign
     */
    void createUser(User user, Set<String> roleGroupNames);

    /**
     * Creates an admin-managed user, assigns role groups, and sends an invitation email.
     *
     * @param user           the user to create
     * @param roleGroupNames names of role groups to assign
     * @return the persisted user
     */
    User createManagedUser(User user, Set<String> roleGroupNames);

    /**
     * Activates a user account using the one-time activation code.
     *
     * @param activationCode the code sent to the user's email
     */
    void activateRegistration(String activationCode);

    /**
     * Resets the user's password using a valid reset code.
     *
     * @param newPassword the new password (plain text)
     * @param resetCode   the one-time reset code
     */
    void completePasswordReset(String newPassword, String resetCode);

    /**
     * Completes account setup for a managed user (via invitation link).
     *
     * @param invitationCode the one-time invitation code
     * @param newPassword    the chosen password (plain text)
     */
    void completeInvitation(String invitationCode, String newPassword);

    /**
     * Initiates a password-reset flow by sending a reset code to the given email.
     *
     * @param email the account email
     */
    void requestPasswordReset(String email);

    /**
     * Re-sends an activation code to the given email.
     *
     * @param email the account email
     */
    void sendActivationCode(String email);

    /**
     * Updates profile information for the user with the given email.
     *
     * @param email  the account email
     * @param update the new profile data
     * @return the updated user
     */
    User updateUser(String email, UserInfoUpdate update);

    /**
     * Changes the current authenticated user's password.
     *
     * @param currentPassword the existing password for verification
     * @param newPassword     the new password (plain text)
     */
    void changePassword(String currentPassword, String newPassword);

    /**
     * Returns the currently authenticated user with their resolved permissions.
     *
     * @return the current user
     */
    User getCurrentUserWithAuthorities();

    /**
     * Returns the user with the given ID and their resolved permissions.
     *
     * @param id the user identifier
     * @return the user
     */
    User getUserWithAuthoritiesById(Long id);

    /**
     * Updates the current authenticated user's own profile.
     *
     * @param update the new profile data
     * @return the updated user
     */
    User updateCurrentUser(UserInfoUpdate update);

    /**
     * Updates the profile of the user with the given ID (admin operation).
     *
     * @param id     the user identifier
     * @param update the new profile data
     * @return the updated user
     */
    User updateUserById(Long id, UserInfoUpdate update);

    /**
     * Soft-deletes the user with the given ID.
     *
     * @param id the user identifier
     */
    void deleteUserById(Long id);

    /**
     * Removes users whose activation has expired (scheduled cleanup).
     */
    void removeNotActivatedUsers();

    /**
     * Soft-deletes the currently authenticated user's account.
     */
    void deleteCurrentUserAccount();

    /**
     * Recovers (reactivates) a previously deactivated account.
     *
     * @param email    the account email
     * @param password the current password for verification
     */
    void recoverAccount(String email, String password);

    /**
     * Permanently deletes soft-deleted users past their retention period (scheduled cleanup).
     */
    void removeSoftDeletedUsers();

    /**
     * Initiates an email-change flow by generating an OTP and sending it to the new email address.
     *
     * @param newEmail the desired new email address
     */
    void requestEmailChange(String newEmail);

    /**
     * Confirms an email-change request by validating the OTP code.
     * On success the user's email is updated and all auth tokens are invalidated.
     *
     * @param code the OTP code sent to the new email address
     */
    void confirmEmailChange(String code);
}
