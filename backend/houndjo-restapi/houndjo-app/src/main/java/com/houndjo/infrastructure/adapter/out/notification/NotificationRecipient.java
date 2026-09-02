package com.houndjo.infrastructure.adapter.out.notification;

import static com.houndjo.domain.constants.DomainConstants.DEFAULT_LANGUAGE;

/**
 * Value object carrying recipient information used when sending notification emails.
 *
 * @param firstName      given name of the recipient
 * @param activationCode account activation code (may be {@code null})
 * @param resetCode      password-reset code (may be {@code null})
 * @param email          recipient's email address
 * @param phoneNumber    optional phone number
 * @param languageKey    preferred locale key; defaults to the application default language
 */
public record NotificationRecipient(
        String firstName,
        String activationCode,
        String resetCode,
        String email,
        String phoneNumber,
        String languageKey) {

    public NotificationRecipient(
            String firstName,
            String activationCode,
            String resetCode,
            String email,
            String phoneNumber,
            String languageKey) {
        this.firstName = firstName;
        this.activationCode = activationCode;
        this.resetCode = resetCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.languageKey = languageKey != null ? languageKey : DEFAULT_LANGUAGE;
    }
}
