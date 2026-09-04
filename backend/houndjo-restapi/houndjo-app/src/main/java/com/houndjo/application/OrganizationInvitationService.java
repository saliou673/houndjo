package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.config.ApplicationProperties;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.enumerations.UserGroupConstants;
import com.houndjo.domain.exceptions.*;
import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.user.*;
import com.houndjo.domain.ports.in.OrganizationInvitationUseCase;
import com.houndjo.domain.ports.out.NotificationSenderPort;
import com.houndjo.domain.ports.out.PasswordHasherPort;
import com.houndjo.domain.ports.out.persistenceport.*;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationInvitationService implements OrganizationInvitationUseCase {
    private final OrganizationInvitationPersistencePort invitationPersistence;
    private final OrganizationPersistencePort organizationPersistence;
    private final MembershipPersistencePort membershipPersistence;
    private final UserPersistencePort userPersistence;
    private final RoleGroupPersistencePort roleGroupPersistence;
    private final PasswordHasherPort passwordHasher;
    private final NotificationSenderPort notificationSender;
    private final ApplicationProperties properties;
    private final TenantContext tenantContext;

    @Override
    public OrganizationInvitation invite(Long organizationId, String email, OrganizationRole role) {
        requireActiveOrganization(organizationId);
        Organization org = organizationPersistence
                .findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        if (invitationPersistence.existsPendingByOrganizationIdAndEmail(organizationId, email)) {
            throw new InvitationAlreadyPendingException(email);
        }
        String code;
        do {
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        } while (invitationPersistence.existsByCode(code));
        OrganizationInvitation invitation = OrganizationInvitation.create(
                organizationId,
                email,
                role,
                code,
                Instant.now().plus(properties.getAccount().managedUserInvitationCodeValidityPeriod()));
        OrganizationInvitation saved = invitationPersistence.save(invitation);
        notificationSender.sendOrganizationInvitationNotification(saved.getEmail(), code, org.getDefaultLanguageKey());
        return saved;
    }

    @Override
    public OrganizationInvitation.AcceptanceResult accept(String code, String password) {
        OrganizationInvitation invitation =
                invitationPersistence.findByCode(code).orElseThrow(() -> new InvitationNotFoundException(code));
        if (invitation.getStatus() == com.houndjo.domain.enumerations.InvitationStatus.REVOKED)
            throw new InvitationRevokedException();
        if (invitation.getStatus() == com.houndjo.domain.enumerations.InvitationStatus.ACCEPTED)
            return new OrganizationInvitation.AcceptanceResult(invitation.getOrganizationId(), invitation.getRole());
        if (invitation.isExpired()) {
            invitation.expire();
            invitationPersistence.save(invitation);
            throw new InvitationExpiredException();
        }
        User user =
                userPersistence.findByEmail(invitation.getEmail()).orElseGet(() -> createUser(invitation, password));
        membershipPersistence
                .findByUserIdAndOrganizationId(user.getId(), invitation.getOrganizationId())
                .ifPresentOrElse(
                        existing -> {
                            existing.activate();
                            if (invitation.getRole().ordinal() < existing.getRole().ordinal()) {
                                existing.changeRole(invitation.getRole());
                            }
                            membershipPersistence.save(existing);
                        },
                        () -> membershipPersistence.save(com.houndjo.domain.models.membership.Membership.create(
                                user.getId(), invitation.getOrganizationId(), invitation.getRole())));
        invitation.accept();
        invitationPersistence.save(invitation);
        return new OrganizationInvitation.AcceptanceResult(invitation.getOrganizationId(), invitation.getRole());
    }

    private User createUser(OrganizationInvitation invitation, String password) {
        if (password == null || password.isBlank()) {
            throw new InvitationPasswordRequiredException();
        }
        String local = invitation.getEmail().substring(0, invitation.getEmail().indexOf('@'));
        User user = User.create(
                new UserInfo(local, "User", null, null, null, null, "fr", null),
                new UserCredentials(
                        invitation.getEmail(),
                        passwordHasher.hash(password),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        user.assignRoleGroups(roleGroupPersistence.findByNames(java.util.Set.of(UserGroupConstants.USER)));
        user.activate(Instant.now());
        return userPersistence.save(user);
    }

    @Override
    public PagedResult<OrganizationInvitation> listPending(Long organizationId, int page, int size) {
        requireActiveOrganization(organizationId);
        return invitationPersistence.findPendingByOrganizationId(organizationId, page, size);
    }

    @Override
    public void revoke(Long organizationId, Long invitationId) {
        requireActiveOrganization(organizationId);
        OrganizationInvitation i = invitationPersistence
                .findByIdAndOrganizationId(invitationId, organizationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));
        i.revoke();
        invitationPersistence.save(i);
    }

    private void requireActiveOrganization(Long organizationId) {
        if (!tenantContext.requireCurrentOrganizationId().equals(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
    }
}
