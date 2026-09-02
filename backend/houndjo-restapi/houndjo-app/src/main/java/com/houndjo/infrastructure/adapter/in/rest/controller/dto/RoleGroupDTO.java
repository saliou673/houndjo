package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "RoleGroup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
/** Response DTO representing a role group with its assigned permissions. */
public class RoleGroupDTO extends AuditableDTO {

    /**
     * Unique identifier of the role group.
     */
    private Long id;
    /**
     * Display name of the role group.
     */
    private String name;
    /**
     * Optional description of the role group's purpose.
     */
    private String description;
    /**
     * Permissions assigned to this role group.
     */
    private List<PermissionDTO> permissions;

    public RoleGroupDTO(
            Long id,
            String name,
            String description,
            List<PermissionDTO> permissions,
            Instant creationDate,
            Instant lastUpdateDate,
            String lastUpdatedBy) {
        super(creationDate, lastUpdateDate, lastUpdatedBy);
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }
}
