package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.progress.ProgressFilter;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.query.PagedResult;
import java.util.Optional;

/**
 * Persistence port for progress records.
 */
public interface ProgressPersistencePort {

    /**
     * Returns the progress records of an organization matching the filter, paginated.
     *
     * @param organizationId the organization identifier
     * @param filter         search criteria
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated progress records
     */
    PagedResult<ProgressRecord> findByOrganizationId(Long organizationId, ProgressFilter filter, int page, int size);

    /**
     * Finds a progress record by its identifier within an organization.
     *
     * @param id             the progress record identifier
     * @param organizationId the owning organization identifier
     * @return the matching progress record, or empty if not found
     */
    Optional<ProgressRecord> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Persists or updates a progress record.
     *
     * @param progressRecord the progress record to save
     * @return the saved progress record
     */
    ProgressRecord save(ProgressRecord progressRecord);

    /**
     * Deletes the progress record with the given identifier.
     *
     * @param id the progress record identifier
     */
    void deleteById(Long id);
}
