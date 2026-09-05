package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.progress.ProgressFilter;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.ProgressPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.ProgressRecordEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.ProgressRecordMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.ProgressRecordRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link ProgressPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class ProgressPersistenceAdapter implements ProgressPersistencePort {

    // Wide-open sentinel bounds standing in for "no constraint" on the corresponding side of the
    // date range, well within PostgreSQL's TIMESTAMP range.
    private static final Instant MIN_INSTANT = Instant.parse("0001-01-01T00:00:00Z");
    private static final Instant MAX_INSTANT = Instant.parse("9999-12-31T23:59:59Z");

    private final ProgressRecordRepository progressRecordRepository;
    private final ProgressRecordMapper progressRecordMapper;

    @Override
    public PagedResult<ProgressRecord> findByOrganizationId(
            Long organizationId, ProgressFilter filter, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationDate")));
                    Instant fromDate = toStartInstant(filter == null ? null : filter.fromDate());
                    Instant toDate = toEndInstant(filter == null ? null : filter.toDate());
                    Page<ProgressRecordEntity> entityPage = progressRecordRepository.search(
                            organizationId,
                            filter == null ? null : filter.studentId(),
                            filter == null ? null : filter.courseId(),
                            filter == null ? null : filter.flow(),
                            fromDate,
                            toDate,
                            pageRequest);
                    List<ProgressRecord> items = progressRecordMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated progress records");
    }

    @Override
    public Optional<ProgressRecord> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> progressRecordRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(progressRecordMapper::toDomain),
                "Error fetching progress record by id");
    }

    @Override
    @Transactional
    public ProgressRecord save(ProgressRecord progressRecord) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> progressRecordMapper.toDomain(
                        progressRecordRepository.save(progressRecordMapper.toEntity(progressRecord))),
                "Error saving progress record");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> progressRecordRepository.deleteById(id), "Error deleting progress record");
    }

    private Instant toStartInstant(LocalDate date) {
        return date == null ? MIN_INSTANT : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant toEndInstant(LocalDate date) {
        return date == null
                ? MAX_INSTANT
                : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);
    }
}
