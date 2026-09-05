package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.FeeType;
import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.domain.models.query.PagedResult;
import java.math.BigDecimal;

/**
 * Use case for managing fee schedules (registration/tuition) within the active organization.
 */
public interface FeeScheduleUseCase {

    /**
     * Returns the fee schedules of the active organization, paginated.
     *
     * @param page zero-based page index
     * @param size maximum items per page
     * @return paginated fee schedules
     */
    PagedResult<FeeSchedule> findAll(int page, int size);

    /**
     * Returns a fee schedule by its identifier within the active organization.
     *
     * @param id the fee schedule identifier
     * @return the matching fee schedule
     */
    FeeSchedule getById(Long id);

    /**
     * Creates a new fee schedule in the active organization. When {@code currencyCode} is blank,
     * it is inherited from the organization's default currency.
     *
     * @param type         the fee type
     * @param amount       the fee amount
     * @param currencyCode the currency code, or blank to inherit the organization's default
     * @param label        display label
     * @return the created fee schedule
     */
    FeeSchedule create(FeeType type, BigDecimal amount, String currencyCode, String label);

    /**
     * Updates an existing fee schedule of the active organization. When {@code currencyCode} is
     * blank, it is inherited from the organization's default currency.
     *
     * @param id           the fee schedule identifier
     * @param type         new fee type
     * @param amount       new amount
     * @param currencyCode new currency code, or blank to inherit the organization's default
     * @param label        new display label
     * @param active       new active flag
     * @return the updated fee schedule
     */
    FeeSchedule update(Long id, FeeType type, BigDecimal amount, String currencyCode, String label, boolean active);

    /**
     * Deletes a fee schedule of the active organization.
     *
     * @param id the fee schedule identifier
     */
    void delete(Long id);
}
