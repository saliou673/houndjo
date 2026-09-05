package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.FeeSchedulePersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.FeeScheduleEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.FeeScheduleMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.FeeScheduleRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link FeeSchedulePersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class FeeSchedulePersistenceAdapter implements FeeSchedulePersistencePort {

    private final FeeScheduleRepository feeScheduleRepository;
    private final FeeScheduleMapper feeScheduleMapper;

    @Override
    public PagedResult<FeeSchedule> findByOrganizationId(Long organizationId, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    Page<FeeScheduleEntity> entityPage = feeScheduleRepository.findByOrganizationId(
                            organizationId, PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationDate"))));
                    List<FeeSchedule> items = feeScheduleMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated fee schedules");
    }

    @Override
    public Optional<FeeSchedule> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> feeScheduleRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(feeScheduleMapper::toDomain),
                "Error fetching fee schedule by id");
    }

    @Override
    @Transactional
    public FeeSchedule save(FeeSchedule feeSchedule) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> feeScheduleMapper.toDomain(
                        feeScheduleRepository.save(feeScheduleMapper.toEntity(feeSchedule))),
                "Error saving fee schedule");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> feeScheduleRepository.deleteById(id), "Error deleting fee schedule");
    }
}
