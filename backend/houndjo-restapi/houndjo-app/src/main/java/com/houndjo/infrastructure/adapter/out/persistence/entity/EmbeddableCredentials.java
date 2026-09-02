package com.houndjo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.*;

/**
 * Embeddable JPA component for user authentication credentials.
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmbeddableCredentials {
    /**
     * Unique email address.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * The password hash.
     */
    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * One-time account activation code.
     */
    @Column(name = "activation_code")
    private String activationCode;

    /**
     * Timestamp when the account was activated.
     */
    @Column(name = "activation_date")
    private Instant activationDate;

    /**
     * One-time password-reset code.
     */
    @Column(name = "reset_code")
    private String resetCode;

    /**
     * Timestamp of the last password-reset request.
     */
    @Column(name = "reset_date")
    private Instant resetDate;

    /**
     * New email address pending confirmation (null when not in an email-change flow).
     */
    @Column(name = "pending_email")
    private String pendingEmail;

    /**
     * One-time code to confirm the email change (null when not in an email-change flow).
     */
    @Column(name = "email_change_code")
    private String emailChangeCode;

    /**
     * Timestamp of the last email-change request (null when not in an email-change flow).
     */
    @Column(name = "email_change_code_date")
    private Instant emailChangeCodeDate;
}
