package com.houndjo.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Strongly-typed configuration properties bound to the {@code app.*} namespace.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    /**
     * Public origin of the web application used in email links.
     */
    private String webAppOrigin;
    /**
     * Security and JWT configuration.
     */
    private Security security;
    /**
     * Default admin user creation settings.
     */
    private DefaultUser defaultUser;
    /**
     * Account lifecycle configuration (cleanup cron, retention periods, invitation codes).
     */
    private Account account;
    /**
     * Email notification settings.
     */
    private Mail mail;
    /**
     * Two-factor authentication configuration.
     */
    private TwoFactor twoFactor;
    /**
     * Rate-limiting configuration.
     */
    private RateLimit rateLimit;
    /**
     * Contact form configuration.
     */
    private Contact contact;

    public Jwt getJwt() {
        return this.security.authentication().jwt();
    }

    public record Security(Authentication authentication, Cors cors) {}

    public record Authentication(Jwt jwt) {}

    public record Jwt(String base64Secret, Long tokenValidityInSeconds, Long tokenValidityInSecondsForRememberMe) {}

    public record DefaultUser(boolean create, String password) {}

    public record Account(
            String cleanupCron,
            Duration nonActivatedUserRetentionPeriod,
            Duration softDeletedUserRetentionPeriod,
            Duration resetCodeValidityPeriod,
            int managedUserInvitationCodeLength,
            Duration managedUserInvitationCodeValidityPeriod) {}

    public record Mail(String from, MailRoute routes) {}

    public record MailRoute(
            String accountValidation, String resetPassword, String login, String managedUserInvitation) {}

    public record Cors(List<String> allowedOrigins) {}

    public record TwoFactor(Duration codeValidityPeriod, int codeLength, String totpIssuer) {}

    public record RateLimit(boolean enabled, Auth auth, Api api) {
        public record Auth(int limitForPeriod, Duration limitRefreshPeriod) {}

        public record Api(int limitForPeriod, Duration limitRefreshPeriod) {}
    }

    public record Contact(List<String> recipientEmails) {}
}
