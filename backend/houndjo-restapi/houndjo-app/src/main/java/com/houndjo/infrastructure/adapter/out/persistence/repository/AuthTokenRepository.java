package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.AuthTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link AuthTokenEntity}.
 */
@Transactional(readOnly = true)
public interface AuthTokenRepository extends JpaRepository<AuthTokenEntity, Long> {
    Optional<AuthTokenEntity> findByRefreshToken(String refreshToken);

    Optional<AuthTokenEntity> findByAccessToken(String accessToken);

    void deleteAllByUserId(Long id);

    void deleteByAccessToken(String accessToken);
}
