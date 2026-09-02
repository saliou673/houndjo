package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.rbac.RoleGroup;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserCredentials;
import com.houndjo.domain.models.user.UserInfo;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EmbeddableCredentials;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EmbeddableUserInfo;
import com.houndjo.infrastructure.adapter.out.persistence.entity.RoleGroupEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {RoleGroupMapper.class})
/** MapStruct mapper between {@link com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity} and the {@link com.houndjo.domain.models.user.User} domain model. */
public interface UserMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    @Mapping(target = "roleGroups", ignore = true)
    UserEntity toEntity(User user);

    default User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.rehydrate(
                entity.getId(),
                toDomain(entity.getUserInfo()),
                toDomain(entity.getUserCredentials()),
                entity.getStatus(),
                toRoleGroupsDomain(entity.getRoleGroups()),
                entity.isTwoFactorEnabled(),
                entity.getTwoFactorMethod(),
                entity.getTotpSecret(),
                null,
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    UserInfo toDomain(EmbeddableUserInfo embeddableUserInfo);

    EmbeddableUserInfo toEntity(UserInfo userInfo);

    UserCredentials toDomain(EmbeddableCredentials embeddableCredentials);

    EmbeddableCredentials toEntity(UserCredentials userCredentials);

    // Distinct name avoids Set<X>/Set<Y> erasure clash
    Set<RoleGroup> toRoleGroupsDomain(Set<RoleGroupEntity> entities);
}
