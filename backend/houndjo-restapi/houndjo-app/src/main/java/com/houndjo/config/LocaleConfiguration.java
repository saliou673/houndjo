package com.houndjo.config;

import com.houndjo.infrastructure.security.AuthenticatedUser;
import com.houndjo.infrastructure.security.UserLanguageKeyLookup;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * By default, all users will speak french. The i18n will come in next versions.
 * It ensures that the right locale is used for the user.
 * <p>
 * Authenticated API calls carry a {@link Jwt} principal (via the OAuth2 resource-server
 * filter), not an {@link AuthenticatedUser} — that type is only ever built during the
 * login request itself. The {@code languageKey} is therefore resolved from the JWT
 * subject through {@link UserLanguageKeyLookup}, which caches the result per user.
 */
@Configuration
@RequiredArgsConstructor
public class LocaleConfiguration {

    private final UserLanguageKeyLookup userLanguageKeyLookup;

    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();
                String languageKey = resolveLanguageKey(authentication);

                if (StringUtils.isNotBlank(languageKey)) {
                    return Locale.forLanguageTag(languageKey);
                }

                // Fallback to standard header-based resolution
                return super.resolveLocale(request);
            }
        };
    }

    private String resolveLanguageKey(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return switch (authentication.getPrincipal()) {
            case AuthenticatedUser user -> user.getLanguageKey();
            case Jwt jwt -> userLanguageKeyLookup.findByEmail(jwt.getSubject()).orElse(null);
            default -> null;
        };
    }
}
