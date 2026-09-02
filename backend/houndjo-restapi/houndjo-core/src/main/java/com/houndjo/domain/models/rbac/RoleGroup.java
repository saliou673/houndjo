package com.houndjo.domain.models.rbac;

import com.houndjo.domain.models.Auditable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 * Aggregate representing a named role group that bundles a set of permissions.
 */
@Getter
public class RoleGroup extends Auditable<Long> {

    /**
     * Unique display name for this role group.
     */
    private String name;
    /**
     * Optional description of the role group's purpose.
     */
    private String description;
    /**
     * Set of permissions granted to members of this group.
     */
    private final Set<Permission> permissions;

    private RoleGroup(
            Long id,
            String name,
            String description,
            Set<Permission> permissions,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(id, creationDate, lastUpdateDate, lastUpdatedBy);
        this.name = name;
        this.description = description;
        this.permissions = permissions == null ? new HashSet<>() : new HashSet<>(permissions);
    }

    public static RoleGroup create(String name, String description, Set<Permission> permissions) {
        return new RoleGroup(null, name, description, permissions, null, null, null);
    }

    public static RoleGroup rehydrate(
            Long id,
            String name,
            String description,
            Set<Permission> permissions,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        return new RoleGroup(id, name, description, permissions, creationDate, lastUpdateDate, lastUpdatedBy);
    }

    public void update(String name, String description, Set<Permission> permissions) {
        this.name = name;
        this.description = description;
        this.permissions.clear();
        if (permissions != null) {
            this.permissions.addAll(permissions);
        }
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}
