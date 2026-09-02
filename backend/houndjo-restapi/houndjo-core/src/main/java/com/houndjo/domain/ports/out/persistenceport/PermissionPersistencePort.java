package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Persistence port for permissions.
 */
public interface PermissionPersistencePort {

    /**
     * Returns all available permissions.
     *
     * @return list of all permissions
     */
    List<Permission> findAll();

    /**
     * Returns permissions using pagination.
     *
     * @param page zero-based page index
     * @param size maximum items per page
     * @return paginated permissions
     */
    PagedResult<Permission> findAll(int page, int size);

    /**
     * Returns the permissions matching the given codes.
     *
     * @param codes collection of permission codes
     * @return set of matching permissions
     */
    Set<Permission> findByCodes(Collection<String> codes);
}
