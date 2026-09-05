package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.models.student.StudentFilter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for students.
 */
public interface StudentPersistencePort {

    /** Batch-load only the requested records belonging to the organization. */
    List<Student> findByIdsAndOrganizationId(Collection<Long> ids, Long organizationId);

    /**
     * Returns the students of an organization matching the filter, paginated.
     *
     * @param organizationId the organization identifier
     * @param filter         search criteria
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated students
     */
    PagedResult<Student> findByOrganizationId(Long organizationId, StudentFilter filter, int page, int size);

    /**
     * Finds a student by its identifier within an organization.
     *
     * @param id             the student identifier
     * @param organizationId the owning organization identifier
     * @return the matching student, or empty if not found
     */
    Optional<Student> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Persists or updates a student.
     *
     * @param student the student to save
     * @return the saved student
     */
    Student save(Student student);

    /**
     * Deletes the student with the given identifier.
     *
     * @param id the student identifier
     */
    void deleteById(Long id);
}
