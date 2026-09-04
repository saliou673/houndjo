package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.CourseEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link CourseEntity}.
 */
@Transactional(readOnly = true)
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    Page<CourseEntity> findByClassIdAndOrganizationId(Long classId, Long organizationId, Pageable pageable);

    Optional<CourseEntity> findByIdAndClassIdAndOrganizationId(Long id, Long classId, Long organizationId);

    List<CourseEntity> findAllByIdInAndClassIdAndOrganizationId(
            Collection<Long> ids, Long classId, Long organizationId);

    @Query("""
            SELECT course.classId AS classId, COUNT(course.id) AS courseCount
            FROM CourseEntity course
            WHERE course.organizationId = :organizationId AND course.classId IN :classIds
            GROUP BY course.classId
            """)
    List<ClassCourseCount> countByClassIdsAndOrganizationId(
            @Param("classIds") Collection<Long> classIds, @Param("organizationId") Long organizationId);

    interface ClassCourseCount {
        Long getClassId();

        long getCourseCount();
    }
}
