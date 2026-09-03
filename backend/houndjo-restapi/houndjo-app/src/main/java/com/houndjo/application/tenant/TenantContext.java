package com.houndjo.application.tenant;

import com.houndjo.domain.exceptions.MissingTenantException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Resolves the organization the current request is scoped to.
 * <p>
 * The active organization is carried by the {@code orgId} claim on the authenticated
 * {@link Jwt} principal. There is no implicit fallback: a request with no such claim has no
 * active organization. See {@code docs/architecture/adr-001-multitenancy.md} for the rationale
 * behind this explicit, application-level guard.
 */
@Component
public class TenantContext {

    public static final String ORGANIZATION_CLAIM = "orgId";

    /**
     * @return the active organization id, or empty if the current request has none.
     */
    public Optional<Long> currentOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        return extractOrganizationId(jwt);
    }

    /**
     * @return the active organization id.
     * @throws MissingTenantException if the current request has no active organization.
     */
    public Long requireCurrentOrganizationId() {
        return currentOrganizationId().orElseThrow(MissingTenantException::new);
    }

    private Optional<Long> extractOrganizationId(Jwt jwt) {
        Object claim = jwt.getClaim(ORGANIZATION_CLAIM);
        return switch (claim) {
            case null -> Optional.empty();
            case Number number -> positiveOrganizationId(number.longValue());
            case String str when !str.isBlank() -> parseOrganizationId(str);
            default -> Optional.empty();
        };
    }

    private Optional<Long> parseOrganizationId(String claim) {
        try {
            return positiveOrganizationId(Long.parseLong(claim));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Long> positiveOrganizationId(long organizationId) {
        return organizationId > 0 ? Optional.of(organizationId) : Optional.empty();
    }
}
