package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.models.enrollment.EnrollmentFilter;
import com.houndjo.domain.models.query.PagedResult;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for enrollments.
 */
public interface EnrollmentPersistencePort {

    /**
     * Returns the enrollments of an organization matching the filter, paginated.
     *
     * @param organizationId the organization identifier
     * @param filter         search criteria
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated enrollments
     */
    PagedResult<Enrollment> findByOrganizationId(Long organizationId, EnrollmentFilter filter, int page, int size);

    /**
     * Finds an enrollment by its identifier within an organization.
     *
     * @param id             the enrollment identifier
     * @param organizationId the owning organization identifier
     * @return the matching enrollment, or empty if not found
     */
    Optional<Enrollment> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Returns every enrollment of a student within an organization.
     *
     * @param studentId      the student identifier
     * @param organizationId the owning organization identifier
     * @return the student's enrollments
     */
    List<Enrollment> findByStudentIdAndOrganizationId(Long studentId, Long organizationId);

    /**
     * Returns whether the student has an {@code ACTIVE} enrollment in the given class.
     *
     * @param studentId      the student identifier
     * @param classId        the class identifier
     * @param organizationId the owning organization identifier
     * @return {@code true} if an active enrollment already exists
     */
    boolean existsActiveByStudentIdAndClassId(Long studentId, Long classId, Long organizationId);

    /**
     * Persists or updates an enrollment.
     *
     * @param enrollment the enrollment to save
     * @return the saved enrollment
     */
    Enrollment save(Enrollment enrollment);
}
