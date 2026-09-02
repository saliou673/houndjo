package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper converting {@link com.houndjo.domain.models.rbac.Permission} to {@link com.houndjo.infrastructure.adapter.in.rest.controller.dto.PermissionDTO}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PermissionDtoMapper {

    PermissionDTO toDTO(Permission permission);

    List<PermissionDTO> toDTO(List<Permission> permissions);
}
