package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.pace.CoursePace;
import java.util.Optional;

/**
 * Persistence port for course pace configurations.
 */
public interface CoursePacePersistencePort {

    /**
     * Finds the pace configuration of a course within an organization.
     *
     * @param courseId       the course identifier
     * @param organizationId the owning organization identifier
     * @return the matching pace, or empty if not configured yet
     */
    Optional<CoursePace> findByCourseIdAndOrganizationId(Long courseId, Long organizationId);

    /**
     * Persists or updates a pace configuration.
     *
     * @param coursePace the pace to save
     * @return the saved pace
     */
    CoursePace save(CoursePace coursePace);

    /**
     * Deletes the pace configuration of a course within an organization.
     *
     * @param courseId       the course identifier
     * @param organizationId the owning organization identifier
     */
    void deleteByCourseIdAndOrganizationId(Long courseId, Long organizationId);
}
