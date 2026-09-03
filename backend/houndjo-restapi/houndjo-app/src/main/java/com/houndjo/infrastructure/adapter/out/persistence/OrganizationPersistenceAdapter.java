package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.ports.out.persistenceport.OrganizationPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.OrganizationMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.OrganizationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link OrganizationPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements OrganizationPersistencePort {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    public Optional<Organization> findById(Long id) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> organizationRepository.findById(id).map(organizationMapper::toDomain),
                "Error fetching organization by id");
    }

    @Override
    public boolean existsBySlug(String slug) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> organizationRepository.existsBySlug(slug), "Error checking organization slug existence");
    }

    @Override
    @Transactional
    public Organization save(Organization organization) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> organizationMapper.toDomain(
                        organizationRepository.save(organizationMapper.toEntity(organization))),
                "Error saving organization");
    }
}
