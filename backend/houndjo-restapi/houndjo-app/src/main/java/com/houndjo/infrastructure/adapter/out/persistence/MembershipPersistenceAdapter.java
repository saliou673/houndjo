package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.enumerations.MembershipStatus;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.MembershipEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.MembershipMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.MembershipRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link MembershipPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class MembershipPersistenceAdapter implements MembershipPersistencePort {

    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    @Override
    public PagedResult<Membership> findByOrganizationId(Long organizationId, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    Page<MembershipEntity> entityPage = membershipRepository.findByOrganizationId(
                            organizationId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate")));
                    List<Membership> items = membershipMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated memberships");
    }

    @Override
    public Optional<Membership> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> membershipRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(membershipMapper::toDomain),
                "Error fetching membership by id and organization");
    }

    @Override
    public Optional<Membership> findByUserIdAndOrganizationId(Long userId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> membershipRepository
                        .findByUserIdAndOrganizationId(userId, organizationId)
                        .map(membershipMapper::toDomain),
                "Error fetching membership by user and organization");
    }

    @Override
    public List<Membership> findActiveByUserId(Long userId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> membershipMapper.toDomain(membershipRepository.findByUserIdAndStatusOrderByCreationDateAsc(
                        userId, MembershipStatus.ACTIVE)),
                "Error fetching active memberships by user");
    }

    @Override
    @Transactional
    public Membership save(Membership membership) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> membershipMapper.toDomain(membershipRepository.save(membershipMapper.toEntity(membership))),
                "Error saving membership");
    }
}
