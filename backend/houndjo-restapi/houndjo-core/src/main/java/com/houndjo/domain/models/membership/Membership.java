package com.houndjo.domain.models.membership;

import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import lombok.Getter;

/**
 * Aggregate linking a user to an organization with an organization-scoped business role.
 */
@Getter
public class Membership extends Auditable<Long> {

    private final Long userId;
    private final Long organizationId;
    private OrganizationRole role;
    private MembershipStatus status;

    private Membership(
            Long id,
            Long userId,
            Long organizationId,
            OrganizationRole role,
            MembershipStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
        this.status = status;
    }

    public static Membership create(Long userId, Long organizationId, OrganizationRole role) {
        return new Membership(null, userId, organizationId, role, MembershipStatus.ACTIVE, null, null, null);
    }

    public static Membership rehydrate(
            Long id,
            Long userId,
            Long organizationId,
            OrganizationRole role,
            MembershipStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new Membership(id, userId, organizationId, role, status, creationDate, lastUpdateDate, lastUpdatedBy);
    }

    public void revoke() {
        this.status = MembershipStatus.REVOKED;
    }

    public void changeRole(OrganizationRole newRole) {
        this.role = newRole;
    }

    public void activate() {
        this.status = MembershipStatus.ACTIVE;
    }
}
