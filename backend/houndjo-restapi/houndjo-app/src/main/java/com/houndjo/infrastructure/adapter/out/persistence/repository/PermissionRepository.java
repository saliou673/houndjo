package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.PermissionEntity;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link PermissionEntity}.
 */
@Transactional(readOnly = true)
public interface PermissionRepository extends JpaRepository<PermissionEntity, String> {

    Set<PermissionEntity> findByCodeIn(Collection<String> codes);
}
