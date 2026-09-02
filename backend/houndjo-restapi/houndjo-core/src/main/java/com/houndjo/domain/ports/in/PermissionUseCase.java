package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.Permission;
import java.util.List;

/**
 * Use case for querying system permissions.
 */
public interface PermissionUseCase {

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
}
