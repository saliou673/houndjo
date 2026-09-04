package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.CourseEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link CourseEntity}.
 */
@Transactional(readOnly = true)
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    Page<CourseEntity> findByClassIdAndOrganizationId(Long classId, Long organizationId, Pageable pageable);

    Optional<CourseEntity> findByIdAndClassIdAndOrganizationId(Long id, Long classId, Long organizationId);
}
