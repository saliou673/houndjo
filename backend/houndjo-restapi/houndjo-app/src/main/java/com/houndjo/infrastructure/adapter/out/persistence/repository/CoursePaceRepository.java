package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.CoursePaceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link CoursePaceEntity}.
 */
@Transactional(readOnly = true)
public interface CoursePaceRepository extends JpaRepository<CoursePaceEntity, Long> {

    Optional<CoursePaceEntity> findByCourseIdAndOrganizationId(Long courseId, Long organizationId);

    @Transactional
    void deleteByCourseIdAndOrganizationId(Long courseId, Long organizationId);
}
