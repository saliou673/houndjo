package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateAppConfigurationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper extracting fields from an {@link com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateAppConfigurationRequest} for use in service calls.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UpdateAppConfigurationRequestMapper {

    default String toCode(UpdateAppConfigurationRequest request) {
        return request.code();
    }

    default String toLabel(UpdateAppConfigurationRequest request) {
        return request.label();
    }

    default String toDescription(UpdateAppConfigurationRequest request) {
        return request.description();
    }

    default boolean toActive(UpdateAppConfigurationRequest request) {
        return Boolean.TRUE.equals(request.active());
    }
}
