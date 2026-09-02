package com.houndjo.infrastructure.security;

import com.houndjo.config.CacheConfiguration;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Resolves a user's preferred {@code languageKey} by email, caching the result so
 * that locale resolution for JWT-authenticated requests doesn't hit the database
 * on every call (see {@code LocaleConfiguration}).
 */
@Component
@RequiredArgsConstructor
public class UserLanguageKeyLookup {

    private final UserRepository userRepository;

    @Cacheable(CacheConfiguration.USER_LANGUAGE_KEY_CACHE)
    public Optional<String> findByEmail(String email) {
        return userRepository
                .findOneByUserCredentialsEmailIgnoreCase(email)
                .map(user -> user.getUserInfo().getLanguageKey());
    }

    @CacheEvict(value = CacheConfiguration.USER_LANGUAGE_KEY_CACHE, key = "#email")
    public void evict(String email) {
        // No-op body: presence of @CacheEvict is what matters.
    }
}
