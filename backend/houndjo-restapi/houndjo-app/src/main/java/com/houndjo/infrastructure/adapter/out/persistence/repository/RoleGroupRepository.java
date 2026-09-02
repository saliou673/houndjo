package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.RoleGroupEntity;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link RoleGroupEntity}.
 */
@Transactional(readOnly = true)
public interface RoleGroupRepository extends JpaRepository<RoleGroupEntity, Long> {

    boolean existsByName(String name);

    Set<RoleGroupEntity> findByNameIn(Collection<String> names);
}
