package com.houndjo.application;

import com.houndjo.domain.exceptions.RoleGroupNameAlreadyExistsException;
import com.houndjo.domain.exceptions.RoleGroupNotFoundException;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.domain.models.rbac.RoleGroup;
import com.houndjo.domain.ports.in.RoleGroupUseCase;
import com.houndjo.domain.ports.out.persistenceport.PermissionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.RoleGroupPersistencePort;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link RoleGroupUseCase}: CRUD and user assignment for role groups.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoleGroupService implements RoleGroupUseCase {

    private final RoleGroupPersistencePort roleGroupPersistencePort;
    private final PermissionPersistencePort permissionPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public List<RoleGroup> findAll() {
        return roleGroupPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<RoleGroup> findAll(int page, int size) {
        return roleGroupPersistencePort.findAll(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleGroup getById(Long id) {
        return roleGroupPersistencePort.findById(id).orElseThrow(() -> new RoleGroupNotFoundException(id));
    }

    @Override
    public RoleGroup create(String name, String description, Set<String> permissionCodes) {
        log.debug("Creating role group: name={}", name);

        if (roleGroupPersistencePort.existsByName(name)) {
            throw new RoleGroupNameAlreadyExistsException(name);
        }

        Set<Permission> permissions = permissionPersistencePort.findByCodes(permissionCodes);
        RoleGroup roleGroup = RoleGroup.create(name, description, permissions);
        return roleGroupPersistencePort.save(roleGroup);
    }

    @Override
    public RoleGroup update(Long id, String name, String description, Set<String> permissionCodes) {
        log.debug("Updating role group id={}", id);

        RoleGroup roleGroup = getById(id);

        if (!roleGroup.getName().equals(name) && roleGroupPersistencePort.existsByName(name)) {
            throw new RoleGroupNameAlreadyExistsException(name);
        }

        Set<Permission> permissions = permissionPersistencePort.findByCodes(permissionCodes);
        roleGroup.update(name, description, permissions);
        return roleGroupPersistencePort.save(roleGroup);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting role group id={}", id);
        getById(id);
        roleGroupPersistencePort.deleteById(id);
    }

    @Override
    public void assignToUser(Long userId, Long roleGroupId) {
        log.debug("Assigning role group {} to user {}", roleGroupId, userId);
        getById(roleGroupId);
        roleGroupPersistencePort.assignToUser(userId, roleGroupId);
    }

    @Override
    public void revokeFromUser(Long userId, Long roleGroupId) {
        log.debug("Revoking role group {} from user {}", roleGroupId, userId);
        getById(roleGroupId);
        roleGroupPersistencePort.revokeFromUser(userId, roleGroupId);
    }
}
