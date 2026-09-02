package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.RoleGroup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Persistence port for role groups.
 */
public interface RoleGroupPersistencePort {

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
     * Returns the role groups matching the given names.
     *
     * @param names collection of role group names
     * @return set of matching role groups
     */
    Set<RoleGroup> findByNames(Collection<String> names);

    /**
     * Finds a role group by its identifier.
     *
     * @param id the role group identifier
     * @return the matching role group, or empty if not found
     */
    Optional<RoleGroup> findById(Long id);

    /**
     * Checks whether a role group with the given name already exists.
     *
     * @param name the name to check
     * @return {@code true} if a role group with that name exists
     */
    boolean existsByName(String name);

    /**
     * Persists or updates a role group.
     *
     * @param roleGroup the role group to save
     * @return the saved role group
     */
    RoleGroup save(RoleGroup roleGroup);

    /**
     * Deletes the role group with the given identifier.
     *
     * @param id the role group identifier
     */
    void deleteById(Long id);

    /**
     * Assigns the given role group to the given user.
     *
     * @param userId      the user identifier
     * @param roleGroupId the role group identifier
     */
    void assignToUser(Long userId, Long roleGroupId);

    /**
     * Revokes the given role group from the given user.
     *
     * @param userId      the user identifier
     * @param roleGroupId the role group identifier
     */
    void revokeFromUser(Long userId, Long roleGroupId);
}
