package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.query.PagedResult;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Persistence port for courses.
 */
public interface CoursePersistencePort {

    /**
     * Returns the courses of a class within an organization, paginated.
     *
     * @param classId        the owning class identifier
     * @param organizationId the organization identifier
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated courses
     */
    PagedResult<Course> findByClassIdAndOrganizationId(Long classId, Long organizationId, int page, int size);

    /**
     * Finds a course by its identifier within a class and organization.
     *
     * @param id             the course identifier
     * @param classId        the owning class identifier
     * @param organizationId the owning organization identifier
     * @return the matching course, or empty if not found
     */
    Optional<Course> findByIdAndClassIdAndOrganizationId(Long id, Long classId, Long organizationId);

    /**
     * Persists or updates a course.
     *
     * @param course the course to save
     * @return the saved course
     */
    Course save(Course course);

    /**
     * Deletes the course with the given identifier.
     *
     * @param id the course identifier
     */
    void deleteById(Long id);

    /**
     * Counts courses for each requested class in one operation.
     *
     * @param classIds       class identifiers to count
     * @param organizationId the owning organization identifier
     * @return counts keyed by class identifier; classes without courses are absent
     */
    Map<Long, Long> countByClassIdsAndOrganizationId(Collection<Long> classIds, Long organizationId);
}
