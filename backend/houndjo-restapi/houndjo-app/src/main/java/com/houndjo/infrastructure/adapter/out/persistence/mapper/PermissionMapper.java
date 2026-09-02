package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.rbac.Permission;
import com.houndjo.infrastructure.adapter.out.persistence.entity.PermissionEntity;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between {@link com.houndjo.infrastructure.adapter.out.persistence.entity.PermissionEntity} and {@link com.houndjo.domain.models.rbac.Permission}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PermissionMapper {

    Permission toDomain(PermissionEntity entity);

    PermissionEntity toEntity(Permission domain);

    List<Permission> toDomain(List<PermissionEntity> entities);

    Set<Permission> toDomain(Set<PermissionEntity> entities);
}
