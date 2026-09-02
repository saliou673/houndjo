package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserCredentials;
import com.houndjo.domain.models.user.UserInfo;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper converting a {@link com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateUserRequest} to domain objects.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CreateUserRequestMapper {

    default User toDomain(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        return User.create(toUserInfo(request), toUserCredentials(request));
    }

    // Mapping for UserInfo
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "languageKey", source = "languageKey")
    @Mapping(target = "imageUrl", source = "imageUrl")
    UserInfo toUserInfo(CreateUserRequest request);

    // Mapping for UserCredentials
    @Mapping(target = "email", source = "email")
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "resetCode", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "activationCode", ignore = true)
    @Mapping(target = "activationDate", ignore = true)
    @Mapping(target = "pendingEmail", ignore = true)
    @Mapping(target = "emailChangeCode", ignore = true)
    @Mapping(target = "emailChangeCodeDate", ignore = true)
    UserCredentials toUserCredentials(CreateUserRequest request);
}
