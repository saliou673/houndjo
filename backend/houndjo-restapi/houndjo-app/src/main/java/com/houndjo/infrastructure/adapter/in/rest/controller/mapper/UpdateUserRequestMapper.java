package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.user.UserInfoUpdate;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper converting an {@link com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateUserRequest} to {@link com.houndjo.domain.models.user.UserInfoUpdate}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UpdateUserRequestMapper {
    UserInfoUpdate toDomain(UpdateUserRequest request);
}
