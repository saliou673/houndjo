package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.enumerations.InvitationStatus;
import com.houndjo.domain.models.organization.OrganizationInvitation;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.OrganizationInvitationPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.OrganizationInvitationEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.OrganizationInvitationMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.OrganizationInvitationRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationInvitationPersistenceAdapter implements OrganizationInvitationPersistencePort {
    private final OrganizationInvitationRepository repository;
    private final OrganizationInvitationMapper mapper;

    public OrganizationInvitation save(OrganizationInvitation i) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> mapper.toDomain(repository.save(mapper.toEntity(i))), "Error saving organization invitation");
    }

    public Optional<OrganizationInvitation> findByCode(String c) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> repository.findByInvitationCode(c).map(mapper::toDomain), "Error fetching invitation");
    }

    public Optional<OrganizationInvitation> findByIdAndOrganizationId(Long id, Long org) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> repository.findByIdAndOrganizationId(id, org).map(mapper::toDomain), "Error fetching invitation");
    }

    public PagedResult<OrganizationInvitation> findPendingByOrganizationId(Long org, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    Page<OrganizationInvitationEntity> p = repository.findByOrganizationIdAndStatus(
                            org,
                            InvitationStatus.PENDING,
                            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate")));
                    return new PagedResult<>(
                            mapper.toDomain(p.getContent()), p.getTotalElements(), page, size, p.getTotalPages());
                },
                "Error fetching pending invitations");
    }

    public boolean existsByCode(String c) {
        return repository.existsByInvitationCode(c);
    }
}
