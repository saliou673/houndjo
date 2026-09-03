package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.organization.Organization;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper converting {@link Organization} to {@link OrganizationDTO}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrganizationDtoMapper {

    OrganizationDTO toDTO(Organization organization);
}
