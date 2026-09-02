package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.domain.models.auth.TwoFactorChallengePurpose;
import com.houndjo.infrastructure.adapter.out.persistence.entity.TwoFactorChallengeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link TwoFactorChallengeEntity}.
 */
public interface TwoFactorChallengeJpaRepository extends JpaRepository<TwoFactorChallengeEntity, String> {

    Optional<TwoFactorChallengeEntity> findByUserIdAndPurpose(Long userId, TwoFactorChallengePurpose purpose);

    void deleteByUserId(Long userId);
}
