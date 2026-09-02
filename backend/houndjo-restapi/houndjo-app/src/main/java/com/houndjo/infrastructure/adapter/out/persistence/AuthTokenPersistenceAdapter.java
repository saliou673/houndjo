package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.auth.AuthToken;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.persistenceport.AuthTokenPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.AuthTokenMapper;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.AuthTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link AuthTokenPersistencePort}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthTokenPersistenceAdapter implements AuthTokenPersistencePort {
    private final AuthTokenRepository authTokenRepository;
    private final AuthTokenMapper authTokenMapper;
    private final UserMapper userMapper;

    @Override
    public Optional<AuthToken> findByRefreshToken(String refreshToken) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> authTokenRepository
                        .findByRefreshToken(refreshToken)
                        .map(entity -> authTokenMapper.toDomain(entity, userMapper)),
                "Error fetching auth token by refresh token");
    }

    @Override
    public Optional<AuthToken> findByAccessToken(String accessToken) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> authTokenRepository
                        .findByAccessToken(accessToken)
                        .map(entity -> authTokenMapper.toDomain(entity, userMapper)),
                "Error fetching auth token by access token");
    }

    @Override
    public void save(AuthToken authToken) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> authTokenRepository.save(authTokenMapper.toEntity(authToken)),
                "Error saving auth token for user: " + authToken.getUser().getId());
    }

    @Override
    public void deleteAllForUser(User user) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> authTokenRepository.deleteAllByUserId(user.getId()),
                "Error deleting auth tokens for user: " + user.getId());
    }

    @Override
    public void deleteByAccessToken(String accessToken) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> authTokenRepository.deleteByAccessToken(accessToken),
                "Error deleting auth token by access token");
    }
}
