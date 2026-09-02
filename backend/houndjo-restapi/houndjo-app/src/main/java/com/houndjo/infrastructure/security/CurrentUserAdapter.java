package com.houndjo.infrastructure.security;

import com.houndjo.domain.ports.out.CurrentUserEmailPort;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing {@link CurrentUserEmailPort} by resolving the email from the Spring Security context.
 */
@Component
public class CurrentUserAdapter implements CurrentUserEmailPort {

    @Override
    public String getCurrentUserEmail() {
        return getSafeCurrentUserEmail();
    }

    private String getSafeCurrentUserEmail() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()))
                .orElseThrow(() -> new UsernameNotFoundException("Current user login not found"));
    }

    private String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return switch (authentication.getPrincipal()) {
            case UserDetails springSecurityUser -> springSecurityUser.getUsername();
            case Jwt jwt -> jwt.getSubject();
            case String str -> str;
            default -> null;
        };
    }
}
