package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.models.enrollment.EnrollmentFilter;
import com.houndjo.domain.models.query.PagedResult;
import java.util.List;
import java.util.Set;

/**
 * Use case for managing enrollments (student ↔ class/course) within the active organization.
 */
public interface EnrollmentUseCase {

    /**
     * Returns the enrollments of the active organization matching the filter, paginated.
     *
     * @param filter search criteria
     * @param page   zero-based page index
     * @param size   maximum items per page
     * @return paginated enrollments
     */
    PagedResult<Enrollment> findAll(EnrollmentFilter filter, int page, int size);

    /**
     * Returns an enrollment by its identifier within the active organization.
     *
     * @param id the enrollment identifier
     * @return the matching enrollment
     */
    Enrollment getById(Long id);

    /**
     * Returns every enrollment of a student within the active organization.
     *
     * @param studentId the student identifier
     * @return the student's enrollments
     */
    List<Enrollment> getByStudent(Long studentId);

    /**
     * Enrolls a student in a class and its courses. Fails if the student already has an active
     * enrollment in the class, or if a course does not belong to the class.
     *
     * @param studentId the student identifier
     * @param classId   the class identifier
     * @param courseIds the courses taken within the class
     * @return the created enrollment
     */
    Enrollment enroll(Long studentId, Long classId, Set<Long> courseIds);

    /**
     * Ends an enrollment.
     *
     * @param id the enrollment identifier
     * @return the ended enrollment
     */
    Enrollment end(Long id);

    /**
     * Adds courses to an enrollment. Fails if a course does not belong to the enrollment's class.
     *
     * @param id        the enrollment identifier
     * @param courseIds the courses to add
     * @return the updated enrollment
     */
    Enrollment addCourses(Long id, Set<Long> courseIds);

    /**
     * Removes courses from an enrollment.
     *
     * @param id        the enrollment identifier
     * @param courseIds the courses to remove
     * @return the updated enrollment
     */
    Enrollment removeCourses(Long id, Set<Long> courseIds);
}
