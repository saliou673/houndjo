package com.houndjo.application;

import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.domain.enumerations.UserGroupConstants;
import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.exceptions.*;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.domain.models.rbac.RoleGroup;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserInfoUpdate;
import com.houndjo.domain.ports.in.AccountUseCase;
import com.houndjo.domain.ports.out.CurrentUserEmailPort;
import com.houndjo.domain.ports.out.NotificationSenderPort;
import com.houndjo.domain.ports.out.PasswordHasherPort;
import com.houndjo.domain.ports.out.persistenceport.AppConfigurationPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.AuthTokenPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.RoleGroupPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.UserPersistencePort;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link AccountUseCase}: user registration, activation, profile, and account recovery.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService implements AccountUseCase {

    public static final int OTP_CODE_SIZE = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    private final UserPersistencePort userPersistencePort;
    private final CurrentUserEmailPort currentUserEmailPort;
    private final RoleGroupPersistencePort roleGroupPersistencePort;
    private final NotificationSenderPort notificationSenderPort;
    private final PasswordHasherPort passwordHasherPort;
    private final ApplicationProperties applicationProperties;
    private final AuthTokenPersistencePort authTokenPersistencePort;
    private final AppConfigurationPersistencePort appConfigurationPersistencePort;

    @Override
    public void createPublicUser(User user) {
        ensureEmailUniqueness(user);
        assignRoleGroups(user, Set.of(UserGroupConstants.USER));
        secureCredentials(user);
        // Auto-activate: public sign-ups get immediate access so users can
        // subscribe right after registration without waiting for an email link.
        user.activate(Instant.now());
        // Still assign a code and send a verification email so the user can
        // confirm ownership of their address at their own pace (non-blocking).
        assignActivationCode(user);
        User saved = userPersistencePort.save(user);
        notificationSenderPort.sendActivationNotification(saved);
    }

    @Override
    public void createUser(User user, Set<String> roleGroupNames) {
        ensureEmailUniqueness(user);
        assignRoleGroups(user, roleGroupNames);
        secureCredentials(user);
        assignActivationCode(user);
        User userCreated = userPersistencePort.save(user);
        notificationSenderPort.sendActivationNotification(userCreated);
    }

    @Override
    public User createManagedUser(User user, Set<String> roleGroupNames) {
        ensureEmailUniqueness(user);
        assignRoleGroups(user, roleGroupNames);
        // Defined a random password to avoid empty value
        user.secureCredentials(passwordHasherPort.hash(UUID.randomUUID().toString()));
        assignInvitationCodeToManagedUser(user);
        if (!isUserOnly(roleGroupNames)) {
            resolveDefaultTwoFactorMethod().ifPresent(user::enableTwoFactor);
        }
        User savedUser = userPersistencePort.save(user);
        notificationSenderPort.sendManagedUserInvitationNotification(savedUser);
        return savedUser;
    }

    @Override
    public void completeInvitation(String invitationCode, String newPassword) {
        User user = userPersistencePort
                .findByResetCode(invitationCode)
                .filter(u -> u.getStatus() == UserStatus.NOT_ACTIVATED)
                .filter(u -> u.getUserCredentials().getResetDate() != null)
                .filter(u -> u.getUserCredentials()
                        .getResetDate()
                        .isAfter(Instant.now().minus(getManagedUserInvitationCodeValidityPeriod())))
                .orElseThrow(InvalidResetCodeException::forInvitation);

        user.changePassword(passwordHasherPort.hash(newPassword), Instant.now());
        user.activate(Instant.now());
        userPersistencePort.save(user);
    }

    @Override
    public void activateRegistration(String activationCode) {
        User user = userPersistencePort
                .findByActivationCode(activationCode)
                .orElseThrow(() -> new ActivationCodeNotFoundException(activationCode));

        try {
            // For regular (non-auto-activated) accounts this marks them active.
            // For auto-activated accounts (public sign-ups) it is a no-op on status,
            // but we still clear the activation code below to confirm email ownership.
            user.activate(Instant.now());
        } catch (UserAlreadyActivatedException ignored) {
            // Auto-activated account: clear the code and stamp the confirmation date.
            log.debug("User with activationCode={} was already active (email verification confirmed)", activationCode);
            user.confirmEmail(Instant.now());
        }

        userPersistencePort.save(user);
        notificationSenderPort.sendCreationNotification(user);
    }

    @Override
    public void completePasswordReset(String newPassword, String resetCode) {
        User user = userPersistencePort
                .findByResetCode(resetCode)
                .filter(u -> u.getUserCredentials().getResetDate() != null)
                .filter(u -> u.getUserCredentials()
                        .getResetDate()
                        .isAfter(Instant.now().minus(getResetCodeValidityPeriod())))
                .orElseThrow(InvalidResetCodeException::new);

        String newPasswordHash = passwordHasherPort.hash(newPassword);
        user.changePassword(newPasswordHash, Instant.now());
        userPersistencePort.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        userPersistencePort
                .findByEmail(email)
                .filter(User::isActive)
                .ifPresentOrElse(
                        user -> {
                            String resetCode = generateUniqueCode(userPersistencePort::existsByResetCode, "reset");
                            user.updateResetCode(resetCode, Instant.now());
                            User savedUser = userPersistencePort.save(user);
                            notificationSenderPort.sendPasswordResetNotification(savedUser);
                        },
                        () -> log.warn("Cannot reset password for unactivated or not found user {}", email));
    }

    @Override
    public void sendActivationCode(String email) {
        User existingUser = getUserByEmailOrThrow(email);

        if (existingUser.isActive()) {
            log.warn("User with email {} is already active. No activation code sent.", email);
            return;
        }

        assignActivationCode(existingUser);
        userPersistencePort.save(existingUser);
        notificationSenderPort.sendActivationNotification(existingUser);
    }

    @Override
    public User updateUser(String email, UserInfoUpdate userInfoUpdate) {
        User existingUser = getUserByEmailOrThrow(email);
        existingUser.updateInfo(userInfoUpdate);
        return userPersistencePort.save(existingUser);
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        String email = getCurrentUserEmail();
        User user = userPersistencePort.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        if (!passwordHasherPort.matches(
                currentPassword, user.getUserCredentials().getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }

        user.secureCredentials(passwordHasherPort.hash(newPassword));
        userPersistencePort.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public User getCurrentUserWithAuthorities() {
        String email = getCurrentUserEmail();
        return userPersistencePort
                .findWithAuthoritiesByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserWithAuthoritiesById(Long id) {
        return getUserByIdOrThrow(id);
    }

    @Override
    public User updateCurrentUser(UserInfoUpdate userInfoUpdate) {
        String email = getCurrentUserEmail();
        return updateUser(email, userInfoUpdate);
    }

    @Override
    public User updateUserById(Long id, UserInfoUpdate userInfoUpdate) {
        User existingUser = getUserByIdOrThrow(id);
        existingUser.updateInfo(userInfoUpdate);
        return userPersistencePort.save(existingUser);
    }

    @Override
    public void deleteUserById(Long id) {
        User existingUser = getUserByIdOrThrow(id);
        userPersistencePort.remove(existingUser);
    }

    @Override
    public void removeNotActivatedUsers() {
        Instant date = Instant.now().minus(getNonActivatedUserRetentionPeriod());
        log.debug("Deleting not activated users since {}", date);
        int count = userPersistencePort.deleteInactiveUsersWithExpiredActivationCode(UserStatus.ACTIVATED, date);
        log.debug("{} not activated users deleted", count);
    }

    @Override
    public void deleteCurrentUserAccount() {
        String email = getCurrentUserEmail();
        User user = getUserByEmailOrThrow(email);

        if (!user.isActive()) {
            throw new AccountNotActivatedException(email);
        }

        user.deactivate();
        User deletedUser = userPersistencePort.save(user);
        authTokenPersistencePort.deleteAllForUser(deletedUser);
        notificationSenderPort.sendAccountDeletionNotification(deletedUser);
    }

    @Override
    public void recoverAccount(String email, String password) {
        User user = getUserByEmailOrThrow(email);

        if (!user.isDeactivated()) {
            throw new FunctionalException(
                    "error.account.recovery-not-available",
                    "Account recovery is available only for deactivated accounts.");
        }

        Instant cutoff = Instant.now().minus(getSoftDeletedUserRetentionPeriod());
        if (user.getLastUpdateDate() != null && user.getLastUpdateDate().isBefore(cutoff)) {
            throw new FunctionalException(
                    "error.account.recovery-period-expired",
                    "The recovery period has expired. Your account has been permanently deleted.");
        }

        if (!passwordHasherPort.matches(password, user.getUserCredentials().getPasswordHash())) {
            throw InvalidCurrentPasswordException.forAccountRecovery();
        }

        user.reactivate();
        userPersistencePort.save(user);
    }

    @Override
    public void removeSoftDeletedUsers() {
        // TODO: Improve this method to check if the user is linked to any data (table) and then really anonymize the
        // user and the set the status to DELETED.
        Instant date = Instant.now().minus(getSoftDeletedUserRetentionPeriod());
        log.info("Deleting soft-deleted users since {}", date);
        int count = userPersistencePort.deleteByStatusAndLastUpdateDateBefore(UserStatus.DEACTIVATED, date);
        log.info("{} soft-deleted users permanently removed", count);
    }

    @Override
    public void requestEmailChange(String newEmail) {
        String currentEmail = getCurrentUserEmail();
        User user = getUserByEmailOrThrow(currentEmail);

        String normalizedNew = newEmail.toLowerCase().trim();
        if (normalizedNew.equals(user.getUserCredentials().getEmail())) {
            throw new FunctionalException(
                    "error.email-change.same-as-current", "The new email must be different from your current email.");
        }

        Instant lastRequest = user.getUserCredentials().getEmailChangeCodeDate();
        if (lastRequest != null && lastRequest.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES))) {
            throw new FunctionalException(
                    "error.email-change.cooldown", "Please wait before requesting another email change code.");
        }

        userPersistencePort.findByEmail(normalizedNew).ifPresent(existingUser -> {
            throw new UserAlreadyExistsException(normalizedNew);
        });

        if (userPersistencePort.existsPendingEmailForAnotherUser(normalizedNew, user.getId())) {
            throw new UserAlreadyExistsException(normalizedNew);
        }

        String code = generateUniqueCode(userPersistencePort::existsByEmailChangeCode, "emailChange");
        user.requestEmailChange(normalizedNew, code, Instant.now());
        User saved = userPersistencePort.save(user);
        notificationSenderPort.sendEmailChangeOtpNotification(saved, normalizedNew);
    }

    @Override
    public void confirmEmailChange(String code) {
        String currentEmail = getCurrentUserEmail();
        User user = getUserByEmailOrThrow(currentEmail);

        if (user.getUserCredentials().getEmailChangeCode() == null
                || !user.getUserCredentials().getEmailChangeCode().equals(code)) {
            throw new FunctionalException("error.email-change.invalid-code", "Invalid email change code.");
        }

        Instant codeDate = user.getUserCredentials().getEmailChangeCodeDate();
        if (codeDate == null || codeDate.isBefore(Instant.now().minus(getResetCodeValidityPeriod()))) {
            throw new FunctionalException("error.email-change.code-expired", "The email change code has expired.");
        }

        String oldEmail = user.getUserCredentials().getEmail();
        user.confirmEmailChange();
        authTokenPersistencePort.deleteAllForUser(user);
        User saved = userPersistencePort.save(user);
        notificationSenderPort.sendEmailChangedOldAddressNotification(saved, oldEmail);
        notificationSenderPort.sendEmailChangedNewAddressNotification(saved);
    }

    private String getCurrentUserEmail() {
        return currentUserEmailPort.getCurrentUserEmail();
    }

    private void assignActivationCode(User user) {
        user.updateActivationCode(generateUniqueCode(userPersistencePort::existsByActivationCode, "activation"));
    }

    private void assignInvitationCodeToManagedUser(User user) {
        int codeLength = applicationProperties.getAccount().managedUserInvitationCodeLength();
        String code = generateUniqueCode(userPersistencePort::existsByResetCode, "invitation", codeLength);
        user.updateResetCode(code, Instant.now());
    }

    private String generateUniqueCode(Predicate<String> existsCodePredicate, String codeType) {
        return generateUniqueCode(existsCodePredicate, codeType, OTP_CODE_SIZE - 1);
    }

    private String generateUniqueCode(Predicate<String> existsCodePredicate, String codeType, int size) {
        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String generatedCode = getRandomCode(size);
            if (!existsCodePredicate.test(generatedCode)) {
                return generatedCode;
            }
            log.warn("Collision on {} code generation at attempt {}", codeType, attempt);
        }
        throw new TechnicalException(
                "error.technical.code-generation-failed",
                "Unable to generate a unique " + codeType + " code after " + MAX_CODE_GENERATION_ATTEMPTS
                        + " attempts.",
                codeType,
                MAX_CODE_GENERATION_ATTEMPTS);
    }

    private static String getRandomCode(int size) {
        int byteCount = (size / 2) + 1;
        byte[] randomBytes = new byte[byteCount];
        SECURE_RANDOM.nextBytes(randomBytes);
        return HEX_FORMAT.formatHex(randomBytes).substring(0, size).toUpperCase(Locale.ROOT);
    }

    private void ensureEmailUniqueness(User user) {
        String email = user.getUserCredentials().getEmail();

        userPersistencePort.findByEmail(email).ifPresent(existingUser -> {
            if (existingUser.isActivationExpired(getNonActivatedUserRetentionPeriod())) {
                log.warn("User with email {} already exists but is not activated. Deleting existing user.", email);
                userPersistencePort.remove(existingUser);
            } else {
                throw new UserAlreadyExistsException(email);
            }
        });
    }

    private void assignRoleGroups(User user, Set<String> roleGroupNames) {
        Set<RoleGroup> roleGroups = roleGroupPersistencePort.findByNames(roleGroupNames);

        if (roleGroups.isEmpty()) {
            throw new IllegalArgumentException("No role groups found for names: " + roleGroupNames);
        }

        user.assignRoleGroups(roleGroups);
    }

    private void secureCredentials(User user) {
        Objects.requireNonNull(user.getUserCredentials());
        user.secureCredentials(passwordHasherPort.hash(user.getUserCredentials().getPasswordHash()));
    }

    private User getUserByEmailOrThrow(String email) {
        return userPersistencePort.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    private TemporalAmount getNonActivatedUserRetentionPeriod() {
        return applicationProperties.getAccount().nonActivatedUserRetentionPeriod();
    }

    private TemporalAmount getSoftDeletedUserRetentionPeriod() {
        return applicationProperties.getAccount().softDeletedUserRetentionPeriod();
    }

    private TemporalAmount getResetCodeValidityPeriod() {
        return applicationProperties.getAccount().resetCodeValidityPeriod();
    }

    private TemporalAmount getManagedUserInvitationCodeValidityPeriod() {
        return applicationProperties.getAccount().managedUserInvitationCodeValidityPeriod();
    }

    private boolean isUserOnly(Set<String> roleGroupNames) {
        return roleGroupNames.size() == 1 && roleGroupNames.contains(UserGroupConstants.USER);
    }

    private Optional<TwoFactorMethodType> resolveDefaultTwoFactorMethod() {
        for (TwoFactorMethodType method : TwoFactorMethodType.values()) {
            if (appConfigurationPersistencePort.existsActiveByCategoryAndCode(
                    AppConfigurationCategory.TWO_FACTOR, method.name())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    private User getUserByIdOrThrow(Long id) {
        return userPersistencePort.findWithAuthoritiesById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
