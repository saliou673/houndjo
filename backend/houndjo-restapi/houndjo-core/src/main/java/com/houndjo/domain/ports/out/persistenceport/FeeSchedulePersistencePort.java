package com.houndjo.domain.ports.out.persistenceport;

import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.domain.models.query.PagedResult;
import java.util.Optional;

/**
 * Persistence port for fee schedules.
 */
public interface FeeSchedulePersistencePort {

    /**
     * Returns the fee schedules of an organization, paginated.
     *
     * @param organizationId the organization identifier
     * @param page           zero-based page index
     * @param size           maximum items per page
     * @return paginated fee schedules
     */
    PagedResult<FeeSchedule> findByOrganizationId(Long organizationId, int page, int size);

    /**
     * Finds a fee schedule by its identifier within an organization.
     *
     * @param id             the fee schedule identifier
     * @param organizationId the owning organization identifier
     * @return the matching fee schedule, or empty if not found
     */
    Optional<FeeSchedule> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Persists or updates a fee schedule.
     *
     * @param feeSchedule the fee schedule to save
     * @return the saved fee schedule
     */
    FeeSchedule save(FeeSchedule feeSchedule);

    /**
     * Deletes the fee schedule with the given identifier.
     *
     * @param id the fee schedule identifier
     */
    void deleteById(Long id);
}
