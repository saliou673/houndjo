package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.auth.TwoFactorChallenge;
import com.houndjo.domain.models.auth.TwoFactorChallengePurpose;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.persistenceport.TwoFactorChallengePersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.TwoFactorChallengeEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.TwoFactorChallengeJpaRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link TwoFactorChallengePersistencePort}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TwoFactorChallengePersistenceAdapter implements TwoFactorChallengePersistencePort {

    private final TwoFactorChallengeJpaRepository repository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public TwoFactorChallenge save(TwoFactorChallenge challenge) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    TwoFactorChallengeEntity entity = toEntity(challenge);
                    return toDomain(repository.save(entity), challenge.getUser());
                },
                "Error saving 2FA challenge for user: " + challenge.getUser().getId());
    }

    @Override
    public Optional<TwoFactorChallenge> findById(String id) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> repository.findById(id).map(entity -> toDomainWithUser(entity)),
                "Error fetching 2FA challenge by id: " + id);
    }

    @Override
    public Optional<TwoFactorChallenge> findByUserIdAndPurpose(Long userId, TwoFactorChallengePurpose purpose) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> repository.findByUserIdAndPurpose(userId, purpose).map(entity -> toDomainWithUser(entity)),
                "Error fetching 2FA challenge for user: " + userId);
    }

    @Override
    public void deleteById(String id) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> repository.deleteById(id), "Error deleting 2FA challenge by id: " + id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> repository.deleteByUserId(userId), "Error deleting 2FA challenges for user: " + userId);
    }

    private TwoFactorChallengeEntity toEntity(TwoFactorChallenge challenge) {
        UserEntity userEntity = userRepository
                .findById(challenge.getUser().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found: " + challenge.getUser().getId()));
        return new TwoFactorChallengeEntity(
                challenge.getId(),
                userEntity,
                challenge.getCode(),
                challenge.getType(),
                challenge.getPurpose(),
                challenge.isRememberMe(),
                challenge.getExpiryDate(),
                challenge.getCreationDate());
    }

    private TwoFactorChallenge toDomainWithUser(TwoFactorChallengeEntity entity) {
        User user = userMapper.toDomain(entity.getUser());
        return toDomain(entity, user);
    }

    private TwoFactorChallenge toDomain(TwoFactorChallengeEntity entity, User user) {
        return TwoFactorChallenge.rehydrate(
                entity.getId(),
                user,
                entity.getCode(),
                entity.getType(),
                entity.getPurpose(),
                entity.isRememberMe(),
                entity.getExpiryDate(),
                entity.getCreationDate());
    }
}
