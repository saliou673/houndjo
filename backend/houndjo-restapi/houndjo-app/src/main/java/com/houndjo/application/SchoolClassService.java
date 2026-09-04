package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.exceptions.SchoolClassNotFoundException;
import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.SchoolClassUseCase;
import com.houndjo.domain.ports.out.persistenceport.SchoolClassPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link SchoolClassUseCase}: CRUD for school classes, scoped
 * to the active organization resolved from {@link TenantContext}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SchoolClassService implements SchoolClassUseCase {

    private final SchoolClassPersistencePort schoolClassPersistencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<SchoolClass> findAll(int page, int size) {
        return schoolClassPersistencePort.findByOrganizationId(
                tenantContext.requireCurrentOrganizationId(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClass getById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    public SchoolClass create(String name, String description, int displayOrder) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        log.debug("Creating class: organizationId={} name={}", organizationId, name);
        SchoolClass schoolClass = SchoolClass.create(organizationId, name, description, displayOrder);
        return schoolClassPersistencePort.save(schoolClass);
    }

    @Override
    public SchoolClass update(Long id, String name, String description, int displayOrder) {
        log.debug("Updating class id={}", id);
        SchoolClass schoolClass = getByIdOrThrow(id);
        schoolClass.update(name, description, displayOrder);
        return schoolClassPersistencePort.save(schoolClass);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting class id={}", id);
        getByIdOrThrow(id);
        schoolClassPersistencePort.deleteById(id);
    }

    private SchoolClass getByIdOrThrow(Long id) {
        return schoolClassPersistencePort
                .findByIdAndOrganizationId(id, tenantContext.requireCurrentOrganizationId())
                .orElseThrow(() -> new SchoolClassNotFoundException(id));
    }
}
