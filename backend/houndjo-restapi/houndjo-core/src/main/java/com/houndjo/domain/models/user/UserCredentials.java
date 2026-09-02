package com.houndjo.domain.models.user;

import static com.houndjo.domain.models.DomainValidation.checkRequiredField;

import com.houndjo.domain.exceptions.UserAlreadyActivatedException;
import com.houndjo.domain.models.DomainValidation;
import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;

/**
 * Holds the authentication credentials for a user account.
 * Mutated only through {@link User} domain methods.
 */
@Getter
public class UserCredentials {

    /**
     * Validated lowercase email address.
     */
    private Email email;
    /**
     * BCrypt hash of the user's password.
     */
    private String passwordHash;
    /**
     * One-time code used to activate the account ({@code null} after activation).
     */
    private String activationCode;
    /**
     * Timestamp when the account was activated ({@code null} until activation).
     */
    private Instant activationDate;
    /**
     * One-time code used to reset the password ({@code null} when not in a reset flow).
     */
    private String resetCode;
    /**
     * Timestamp of the last password-reset request.
     */
    private Instant resetDate;
    /**
     * New email address pending confirmation ({@code null} when not in an email-change flow).
     */
    private String pendingEmail;
    /**
     * One-time code used to confirm an email change ({@code null} when not in an email-change flow).
     */
    private String emailChangeCode;
    /**
     * Timestamp of the last email-change request ({@code null} when not in an email-change flow).
     */
    private Instant emailChangeCodeDate;

    public UserCredentials(
            String email,
            String passwordHash,
            String activationCode,
            Instant activationDate,
            String resetCode,
            Instant resetDate,
            String pendingEmail,
            String emailChangeCode,
            Instant emailChangeCodeDate) {
        this.email = new Email(email);

        checkRequiredField(passwordHash, "passwordHash");

        this.passwordHash = passwordHash;
        this.activationCode = activationCode;
        this.activationDate = activationDate;
        this.resetCode = resetCode;
        this.resetDate = resetDate;
        this.pendingEmail = pendingEmail;
        this.emailChangeCode = emailChangeCode;
        this.emailChangeCodeDate = emailChangeCodeDate;
    }

    protected void changePassword(String newPasswordHash, Instant resetDate) {
        checkRequiredField(newPasswordHash, "passwordHash");
        this.passwordHash = newPasswordHash;
        this.resetDate = resetDate;
        this.resetCode = null;
    }

    protected void hashPassword(String newPasswordHash) {
        checkRequiredField(newPasswordHash, "passwordHash");
        this.passwordHash = newPasswordHash;
    }

    protected void activate(Instant activationDate) {
        if (this.activationDate != null) {
            throw new UserAlreadyActivatedException();
        }
        this.activationDate = activationDate;
        this.activationCode = null;
    }

    protected void updateActivationCode(String newActivationCode) {
        DomainValidation.checkRequiredField(newActivationCode, "activationCode");
        this.activationCode = newActivationCode;
        this.activationDate = null;
    }

    protected void confirmEmail(Instant confirmationDate) {
        this.activationCode = null;
        this.activationDate = confirmationDate;
    }

    protected void updateResetCode(String newResetCode, @NonNull Instant resetDate) {
        checkRequiredField(newResetCode, "resetCode");
        this.resetCode = newResetCode;
        this.resetDate = resetDate;
    }

    public String getEmail() {
        return email.value();
    }

    protected void requestEmailChange(String newEmail, String code, Instant codeDate) {
        Email validated = new Email(newEmail);
        this.pendingEmail = validated.value();
        this.emailChangeCode = code;
        this.emailChangeCodeDate = codeDate;
    }

    protected void confirmEmailChange() {
        this.email = new Email(this.pendingEmail);
        this.pendingEmail = null;
        this.emailChangeCode = null;
        this.emailChangeCodeDate = null;
    }
}
