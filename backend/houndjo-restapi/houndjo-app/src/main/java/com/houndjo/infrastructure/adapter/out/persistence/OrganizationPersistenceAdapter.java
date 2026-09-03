package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.organization.Organization;
import com.houndjo.domain.ports.out.persistenceport.OrganizationPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.OrganizationMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.OrganizationRepository;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
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
    private final EntityManager entityManager;

    @Override
    public Optional<Organization> findById(Long id) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> organizationRepository.findById(id).map(organizationMapper::toDomain),
                "Error fetching organization by id");
    }

    @Override
    public List<Organization> findByIds(Collection<Long> ids) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> organizationMapper.toDomain(organizationRepository.findAllById(ids)),
                "Error fetching organizations by ids");
    }

    @Override
    public boolean existsBySlug(String slug) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> organizationRepository.existsBySlug(slug), "Error checking organization slug existence");
    }

    @Override
    @Transactional
    public void acquireSlugAllocationLock(String baseSlug) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> entityManager
                        .createNativeQuery("SELECT pg_advisory_xact_lock(hashtextextended(?1, 0))")
                        .setParameter(1, baseSlug)
                        .getSingleResult(),
                "Error acquiring organization slug allocation lock");
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
