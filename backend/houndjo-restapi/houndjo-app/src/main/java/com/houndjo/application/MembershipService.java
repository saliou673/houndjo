package com.houndjo.application;

import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.exceptions.MembershipNotFoundException;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.MembershipUseCase;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link MembershipUseCase}: organization membership management.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MembershipService implements MembershipUseCase {

    private final MembershipPersistencePort membershipPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Membership> findByOrganizationId(Long organizationId, int page, int size) {
        return membershipPersistencePort.findByOrganizationId(organizationId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Membership> findActiveByUserId(Long userId) {
        return membershipPersistencePort.findActiveByUserId(userId);
    }

    @Override
    public Membership changeRole(Long id, OrganizationRole newRole) {
        log.debug("Changing role of membership id={} to {}", id, newRole);
        Membership membership = getByIdOrThrow(id);
        membership.changeRole(newRole);
        return membershipPersistencePort.save(membership);
    }

    @Override
    public void revoke(Long id) {
        log.debug("Revoking membership id={}", id);
        Membership membership = getByIdOrThrow(id);
        membership.revoke();
        membershipPersistencePort.save(membership);
    }

    private Membership getByIdOrThrow(Long id) {
        return membershipPersistencePort.findById(id).orElseThrow(() -> new MembershipNotFoundException(id));
    }
}
