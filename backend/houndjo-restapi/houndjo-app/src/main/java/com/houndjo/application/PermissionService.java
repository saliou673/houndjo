package com.houndjo.application;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.ports.in.PermissionUseCase;
import com.houndjo.domain.ports.out.persistenceport.PermissionPersistencePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link PermissionUseCase}: read-only permission queries.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionService implements PermissionUseCase {

    private final PermissionPersistencePort permissionPersistencePort;

    @Override
    public List<Permission> findAll() {
        return permissionPersistencePort.findAll();
    }

    @Override
    public PagedResult<Permission> findAll(int page, int size) {
        return permissionPersistencePort.findAll(page, size);
    }
}
