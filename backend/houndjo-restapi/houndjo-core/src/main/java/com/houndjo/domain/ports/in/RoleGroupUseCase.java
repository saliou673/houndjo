package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.RoleGroup;
import java.util.List;
import java.util.Set;

/**
 * Use case for managing role groups and their assignment to users.
 */
public interface RoleGroupUseCase {

    /**
     * Returns all role groups.
     *
     * @return list of all role groups
     */
    List<RoleGroup> findAll();

    /**
     * Returns role groups using pagination.
     *
     * @param page zero-based page index
     * @param size maximum items per page
     * @return paginated role groups
     */
    PagedResult<RoleGroup> findAll(int page, int size);

    /**
     * Returns a role group by its identifier.
     *
     * @param id the role group identifier
     * @return the role group
     */
    RoleGroup getById(Long id);

    /**
     * Creates a new role group with the specified permissions.
     *
     * @param name            unique name
     * @param description     human-readable description
     * @param permissionCodes codes of permissions to assign
     * @return the created role group
     */
    RoleGroup create(String name, String description, Set<String> permissionCodes);

    /**
     * Updates an existing role group.
     *
     * @param id              the role group identifier
     * @param name            new name
     * @param description     new description
     * @param permissionCodes new set of permission codes
     * @return the updated role group
     */
    RoleGroup update(Long id, String name, String description, Set<String> permissionCodes);

    /**
     * Deletes the role group with the given identifier.
     *
     * @param id the role group identifier
     */
    void delete(Long id);

    /**
     * Assigns a role group to a user.
     *
     * @param userId      the user identifier
     * @param roleGroupId the role group identifier
     */
    void assignToUser(Long userId, Long roleGroupId);

    /**
     * Revokes a role group from a user.
     *
     * @param userId      the user identifier
     * @param roleGroupId the role group identifier
     */
    void revokeFromUser(Long userId, Long roleGroupId);
}
