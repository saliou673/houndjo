package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.rbac.RoleGroup;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.RoleGroupDTO;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper converting {@link com.houndjo.domain.models.rbac.RoleGroup} to {@link com.houndjo.infrastructure.adapter.in.rest.controller.dto.RoleGroupDTO}.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = PermissionDtoMapper.class)
public interface RoleGroupDtoMapper {

    RoleGroupDTO toDTO(RoleGroup roleGroup);

    List<RoleGroupDTO> toDTO(List<RoleGroup> roleGroups);

    List<RoleGroupDTO> toDTO(Set<RoleGroup> roleGroups);
}
