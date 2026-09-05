package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.FeeType;
import com.houndjo.domain.exceptions.FeeScheduleNotFoundException;
import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.FeeScheduleUseCase;
import com.houndjo.domain.ports.in.OrganizationUseCase;
import com.houndjo.domain.ports.out.persistenceport.FeeSchedulePersistencePort;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link FeeScheduleUseCase}: CRUD for fee schedules, scoped to
 * the active organization resolved from {@link TenantContext}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FeeScheduleService implements FeeScheduleUseCase {

    private final FeeSchedulePersistencePort feeSchedulePersistencePort;
    private final OrganizationUseCase organizationUseCase;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<FeeSchedule> findAll(int page, int size) {
        return feeSchedulePersistencePort.findByOrganizationId(
                tenantContext.requireCurrentOrganizationId(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeSchedule getById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    public FeeSchedule create(FeeType type, BigDecimal amount, String currencyCode, String label) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        log.debug("Creating fee schedule: organizationId={} type={}", organizationId, type);
        FeeSchedule feeSchedule =
                FeeSchedule.create(organizationId, type, amount, resolveCurrencyCode(currencyCode, organizationId), label);
        return feeSchedulePersistencePort.save(feeSchedule);
    }

    @Override
    public FeeSchedule update(
            Long id, FeeType type, BigDecimal amount, String currencyCode, String label, boolean active) {
        log.debug("Updating fee schedule id={}", id);
        FeeSchedule feeSchedule = getByIdOrThrow(id);
        feeSchedule.update(
                type, amount, resolveCurrencyCode(currencyCode, feeSchedule.getOrganizationId()), label, active);
        return feeSchedulePersistencePort.save(feeSchedule);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting fee schedule id={}", id);
        getByIdOrThrow(id);
        feeSchedulePersistencePort.deleteById(id);
    }

    private String resolveCurrencyCode(String currencyCode, Long organizationId) {
        return currencyCode == null || currencyCode.isBlank()
                ? organizationUseCase.getById(organizationId).getDefaultCurrencyCode()
                : currencyCode;
    }

    private FeeSchedule getByIdOrThrow(Long id) {
        return feeSchedulePersistencePort
                .findByIdAndOrganizationId(id, tenantContext.requireCurrentOrganizationId())
                .orElseThrow(() -> new FeeScheduleNotFoundException(id));
    }
}
