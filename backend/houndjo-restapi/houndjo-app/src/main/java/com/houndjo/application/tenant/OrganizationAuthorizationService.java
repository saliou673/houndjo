package com.houndjo.application.tenant;

import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.CurrentUserEmailPort;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.UserPersistencePort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SpEL-exposed {@code @authz} bean checking the current user's organization-scoped role,
 * resolved against {@link TenantContext}'s active organization. See
 * {@code docs/architecture/adr-001-multitenancy.md}.
 */
@Component("authz")
@RequiredArgsConstructor
public class OrganizationAuthorizationService {

    private final TenantContext tenantContext;
    private final CurrentUserEmailPort currentUserEmailPort;
    private final UserPersistencePort userPersistencePort;
    private final MembershipPersistencePort membershipPersistencePort;

    /**
     * @param roles the {@link OrganizationRole} names to accept
     * @return {@code true} if the current user has an active membership with one of the given
     * roles in the currently active organization; {@code false} if there is no active
     * organization, no current user, or no matching membership.
     */
    public boolean hasOrgRole(String... roles) {
        return tenantContext
                .currentOrganizationId()
                .flatMap(this::currentActiveMembership)
                .map(membership -> matchesAnyRole(membership.getRole(), roles))
                .orElse(false);
    }

    private Optional<Membership> currentActiveMembership(Long organizationId) {
        return currentUserId()
                .flatMap(userId -> membershipPersistencePort.findByUserIdAndOrganizationId(userId, organizationId))
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE);
    }

    private Optional<Long> currentUserId() {
        return userPersistencePort
                .findByEmail(currentUserEmailPort.getCurrentUserEmail())
                .map(User::getId);
    }

    private boolean matchesAnyRole(OrganizationRole actual, String[] roles) {
        for (String role : roles) {
            if (actual.name().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
