package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.InvitationStatus;
import com.houndjo.domain.enumerations.OrganizationRole;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
        name = "organization_invitation",
        uniqueConstraints = @UniqueConstraint(name = "uq_org_invitation_code", columnNames = "invitation_code"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrganizationInvitationEntity extends AuditableEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationRole role;

    @Column(name = "invitation_code", nullable = false, columnDefinition = "TEXT")
    private String invitationCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private InvitationStatus status;

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof OrganizationInvitationEntity e && id != null && id.equals(e.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
