package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.domain.models.query.PagedResult;

/**
 * Use case for managing school classes (grade/class levels) within the active organization.
 */
public interface SchoolClassUseCase {

    /**
     * Returns the classes of the active organization, paginated.
     *
     * @param page zero-based page index
     * @param size maximum items per page
     * @return paginated classes
     */
    PagedResult<SchoolClass> findAll(int page, int size);

    /**
     * Returns a class by its identifier within the active organization.
     *
     * @param id the class identifier
     * @return the matching class
     */
    SchoolClass getById(Long id);

    /**
     * Creates a new class in the active organization.
     *
     * @param name         display name
     * @param description  optional description
     * @param displayOrder ordering hint among the organization's classes
     * @return the created class
     */
    SchoolClass create(String name, String description, int displayOrder);

    /**
     * Updates an existing class of the active organization.
     *
     * @param id           the class identifier
     * @param name         new display name
     * @param description  new description
     * @param displayOrder new ordering hint
     * @return the updated class
     */
    SchoolClass update(Long id, String name, String description, int displayOrder);

    /**
     * Deletes a class of the active organization.
     *
     * @param id the class identifier
     */
    void delete(Long id);
}
