package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.persistenceport.UserDetailsPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.UserPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.RoleGroupEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserPreferenceEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.RoleGroupRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserPreferenceRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserRepository;
import com.houndjo.infrastructure.security.UserLanguageKeyLookup;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link UserPersistencePort} and {@link UserDetailsPersistencePort}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort, UserDetailsPersistencePort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleGroupRepository roleGroupRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserLanguageKeyLookup userLanguageKeyLookup;

    @Override
    public User save(User user) {
        User savedUser = AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    UserEntity entity = userMapper.toEntity(user);
                    if (!user.getRoleGroups().isEmpty()) {
                        Set<Long> roleGroupIds = user.getRoleGroups().stream()
                                .map(rg -> rg.getId())
                                .collect(Collectors.toSet());
                        Set<RoleGroupEntity> roleGroupEntities =
                                new HashSet<>(roleGroupRepository.findAllById(roleGroupIds));
                        entity.setRoleGroups(roleGroupEntities);
                    }
                    UserEntity savedEntity = userRepository.save(entity);
                    userPreferenceRepository.save(new UserPreferenceEntity(savedEntity.getId(), user.getPreferences()));
                    return enrichPreferences(userMapper.toDomain(savedEntity));
                },
                "Error saving user with email: " + user.getUserCredentials().getEmail());
        // The languageKey may have changed; evict so locale resolution picks it up immediately.
        userLanguageKeyLookup.evict(savedUser.getUserCredentials().getEmail());
        return savedUser;
    }

    @Override
    public Optional<User> findWithAuthoritiesByEmail(String email) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository
                        .findOneWithAuthoritiesByUserCredentialsEmailIgnoreCase(email)
                        .map(userMapper::toDomain)
                        .map(this::enrichPreferences),
                "Error fetching user with authorities by email: " + email);
    }

    @Override
    public Optional<User> findWithAuthoritiesById(Long id) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository
                        .findOneWithAuthoritiesById(id)
                        .map(userMapper::toDomain)
                        .map(this::enrichPreferences),
                "Error fetching user with authorities by id: " + id);
    }

    @Override
    public Optional<User> findUserWithAuthoritiesByEmail(String email) {
        return findWithAuthoritiesByEmail(email);
    }

    @Override
    public Optional<User> findByActivationCode(String activationCode) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository
                        .findOneByUserCredentialsActivationCode(activationCode)
                        .map(userMapper::toDomain)
                        .map(this::enrichPreferences),
                "Error fetching user by activation code: " + activationCode);
    }

    @Override
    public Optional<User> findByResetCode(String resetCode) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository
                        .findOneByUserCredentialsResetCode(resetCode)
                        .map(userMapper::toDomain)
                        .map(this::enrichPreferences),
                "Error fetching user by reset code: " + resetCode);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository
                        .findOneByUserCredentialsEmailIgnoreCase(email)
                        .map(userMapper::toDomain)
                        .map(this::enrichPreferences),
                "Error fetching user by email: " + email);
    }

    @Override
    public boolean existsByActivationCode(String activationCode) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository.existsByUserCredentialsActivationCode(activationCode),
                "Error checking existing user activation code: " + activationCode);
    }

    @Override
    public boolean existsByResetCode(String resetCode) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository.existsByUserCredentialsResetCode(resetCode),
                "Error checking existing user reset code: " + resetCode);
    }

    @Override
    public int deleteInactiveUsersWithExpiredActivationCode(UserStatus status, Instant dateTime) {
        return AdapterPersistenceUtils.executeDbOperation(
                () ->
                        userRepository
                                .deleteAllByUserCredentialsActivationCodeIsNotNullAndStatusIsNotAndCreationDateBefore(
                                        status, dateTime),
                "Error deleting inactive users with expired activation codes");
    }

    @Override
    public int deleteByStatusAndLastUpdateDateBefore(UserStatus status, Instant dateTime) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository.deleteAllByStatusAndLastUpdateDateBefore(status, dateTime),
                "Error deleting users with status " + status + " before " + dateTime);
    }

    @Override
    public void remove(User existingUser) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository.delete(userMapper.toEntity(existingUser)),
                "Error removing user with email: "
                        + existingUser.getUserCredentials().getEmail());
    }

    @Override
    public Optional<User> findByEmailChangeCode(String code) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository
                        .findOneByUserCredentialsEmailChangeCode(code)
                        .map(userMapper::toDomain)
                        .map(this::enrichPreferences),
                "Error fetching user by email change code: " + code);
    }

    @Override
    public boolean existsByEmailChangeCode(String code) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository.existsByUserCredentialsEmailChangeCode(code),
                "Error checking existing user email change code: " + code);
    }

    @Override
    public boolean existsPendingEmailForAnotherUser(String email, Long excludeUserId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> userRepository.existsByUserCredentialsPendingEmailAndIdNot(email, excludeUserId),
                "Error checking pending email uniqueness for: " + email);
    }

    private User enrichPreferences(User user) {
        userPreferenceRepository
                .findById(user.getId())
                .map(UserPreferenceEntity::getPreferences)
                .ifPresent(user::updatePreferences);
        return user;
    }
}
