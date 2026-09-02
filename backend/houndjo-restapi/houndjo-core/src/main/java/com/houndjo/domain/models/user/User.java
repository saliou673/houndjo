package com.houndjo.domain.models.user;

import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.exceptions.UserAlreadyActivatedException;
import com.houndjo.domain.models.Auditable;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.models.rbac.RoleGroup;
import com.houndjo.domain.models.userpreference.UserPreferences;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;

/**
 * Aggregate root representing an application user.
 */
@Getter
public class User extends Auditable<Long> {

    /**
     * Profile information (name, contact, locale).
     */
    private UserInfo userInfo;
    /**
     * Authentication credentials (email, password hash, codes).
     */
    private final UserCredentials userCredentials;
    /**
     * Current account lifecycle status.
     */
    private UserStatus status;
    /**
     * Role groups assigned to this user.
     */
    private final Set<RoleGroup> roleGroups;
    /**
     * Whether two-factor authentication is active.
     */
    private boolean twoFactorEnabled;
    /**
     * The 2FA method currently configured ({@code null} if 2FA is disabled).
     */
    private TwoFactorMethodType twoFactorMethod;
    /**
     * TOTP shared secret ({@code null} unless method is TOTP).
     */
    private String totpSecret;
    /**
     * User-owned preferences persisted alongside the user aggregate.
     */
    private UserPreferences preferences;

    private User(
            Long id,
            UserInfo userInfo,
            UserCredentials userCredentials,
            UserStatus status,
            Set<RoleGroup> roleGroups,
            boolean twoFactorEnabled,
            TwoFactorMethodType twoFactorMethod,
            String totpSecret,
            UserPreferences preferences,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.userInfo = Objects.requireNonNull(userInfo, "userInfo must not be null");
        this.userCredentials = Objects.requireNonNull(userCredentials, "userCredentials must not be null");
        this.status = status;
        this.roleGroups = roleGroups == null ? new HashSet<>() : new HashSet<>(roleGroups);
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorMethod = twoFactorMethod;
        this.totpSecret = totpSecret;
        this.preferences = preferences == null ? UserPreferences.defaults() : preferences;
    }

    public static User create(UserInfo userInfo, UserCredentials userCredentials) {
        return new User(
                null,
                userInfo,
                userCredentials,
                UserStatus.NOT_ACTIVATED,
                new HashSet<>(),
                false,
                null,
                null,
                UserPreferences.defaults(),
                null,
                null,
                null);
    }

    public static User rehydrate(
            Long id,
            UserInfo userInfo,
            UserCredentials userCredentials,
            UserStatus status,
            Set<RoleGroup> roleGroups,
            boolean twoFactorEnabled,
            TwoFactorMethodType twoFactorMethod,
            String totpSecret,
            UserPreferences preferences,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new User(
                id,
                userInfo,
                userCredentials,
                status,
                roleGroups,
                twoFactorEnabled,
                twoFactorMethod,
                totpSecret,
                preferences,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public Set<RoleGroup> getRoleGroups() {
        return Collections.unmodifiableSet(roleGroups);
    }

    /**
     * Returns the flat set of permissions resolved from all assigned role groups.
     */
    public Set<Permission> resolvePermissions() {
        return roleGroups.stream().flatMap(rg -> rg.getPermissions().stream()).collect(Collectors.toUnmodifiableSet());
    }

    public void updateInfo(UserInfoUpdate newUserInfo) {
        this.userInfo = this.userInfo.updateInfo(newUserInfo);
    }

    public boolean isActivationExpired(TemporalAmount nonActivatedUserRetentionPeriod) {
        return UserStatus.NOT_ACTIVATED.equals(this.status)
                && getCreationDate() != null
                && getCreationDate().isBefore(Instant.now().minus(nonActivatedUserRetentionPeriod));
    }

    public boolean isActive() {
        return UserStatus.ACTIVATED.equals(this.status);
    }

    public boolean isDeactivated() {
        return UserStatus.DEACTIVATED.equals(this.status);
    }

    public void activate(@NonNull Instant activationDate) {
        if (this.isActive()) {
            throw new UserAlreadyActivatedException();
        }

        this.userCredentials.activate(activationDate);
        this.status = UserStatus.ACTIVATED;
    }

    public void deactivate() {
        this.status = UserStatus.DEACTIVATED;
    }

    public void reactivate() {
        this.status = UserStatus.ACTIVATED;
    }

    public void assignRoleGroups(Set<RoleGroup> roleGroups) {
        if (roleGroups == null || roleGroups.isEmpty()) {
            return;
        }
        this.roleGroups.addAll(roleGroups);
    }

    public void secureCredentials(String passwordHash) {
        this.userCredentials.hashPassword(passwordHash);
    }

    public void updateActivationCode(String activationCode) {
        this.userCredentials.updateActivationCode(activationCode);
    }

    public void confirmEmail(Instant confirmationDate) {
        this.userCredentials.confirmEmail(confirmationDate);
    }

    public void updateResetCode(String resetCode, Instant resetDate) {
        this.userCredentials.updateResetCode(resetCode, resetDate);
    }

    public void changePassword(String newPasswordHash, Instant resetDate) {
        this.userCredentials.changePassword(newPasswordHash, resetDate);
    }

    public void enableTwoFactor(TwoFactorMethodType method) {
        this.twoFactorEnabled = true;
        this.twoFactorMethod = Objects.requireNonNull(method, "method must not be null");
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public void disableTwoFactor() {
        this.twoFactorEnabled = false;
        this.twoFactorMethod = null;
        this.totpSecret = null;
    }

    public void updatePreferences(UserPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences must not be null");
    }

    public void requestEmailChange(String newEmail, String code, Instant codeDate) {
        this.userCredentials.requestEmailChange(newEmail, code, codeDate);
    }

    public void confirmEmailChange() {
        this.userCredentials.confirmEmailChange();
    }
}
