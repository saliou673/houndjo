package com.houndjo.domain.models.organization;

import com.houndjo.domain.enumerations.InvitationStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import lombok.Getter;

@Getter
public class OrganizationInvitation extends Auditable<Long> {
    public record AcceptanceResult(Long organizationId, OrganizationRole role) {}

    private final Long organizationId;
    private final String email;
    private final OrganizationRole role;
    private final String invitationCode;
    private final Instant expiresAt;
    private InvitationStatus status;

    private OrganizationInvitation(
            Long id,
            Long organizationId,
            String email,
            OrganizationRole role,
            String invitationCode,
            Instant expiresAt,
            InvitationStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.organizationId = organizationId;
        this.email = email;
        this.role = role;
        this.invitationCode = invitationCode;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public static OrganizationInvitation create(
            Long organizationId, String email, OrganizationRole role, String invitationCode, Instant expiresAt) {
        return new OrganizationInvitation(
                null,
                organizationId,
                email.toLowerCase().trim(),
                role,
                invitationCode,
                expiresAt,
                InvitationStatus.PENDING,
                null,
                null,
                null);
    }

    public static OrganizationInvitation rehydrate(
            Long id,
            Long organizationId,
            String email,
            OrganizationRole role,
            String invitationCode,
            Instant expiresAt,
            InvitationStatus status,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new OrganizationInvitation(
                id,
                organizationId,
                email,
                role,
                invitationCode,
                expiresAt,
                status,
                creationDate,
                lastUpdateDate,
                lastUpdatedBy);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void accept() {
        status = InvitationStatus.ACCEPTED;
    }

    public void revoke() {
        status = InvitationStatus.REVOKED;
    }

    public void expire() {
        status = InvitationStatus.EXPIRED;
    }
}
