package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.domain.models.query.PagedResult;
import java.util.Optional;

/**
 * Persistence port for school classes.
 */
public interface SchoolClassPersistencePort {

    /**
     * Returns the classes of an organization, paginated.
     *
     * @param organizationId the organization identifier
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated classes
     */
    PagedResult<SchoolClass> findByOrganizationId(Long organizationId, int page, int size);

    /**
     * Finds a class by its identifier within an organization.
     *
     * @param id             the class identifier
     * @param organizationId the owning organization identifier
     * @return the matching class, or empty if not found
     */
    Optional<SchoolClass> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Persists or updates a class.
     *
     * @param schoolClass the class to save
     * @return the saved class
     */
    SchoolClass save(SchoolClass schoolClass);

    /**
     * Deletes the class with the given identifier.
     *
     * @param id the class identifier
     */
    void deleteById(Long id);
}
