package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.enumerations.EnrollmentStatus;
import com.houndjo.domain.exceptions.DataBaseException;
import com.houndjo.domain.exceptions.DuplicateActiveEnrollmentException;
import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.models.enrollment.EnrollmentFilter;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.EnrollmentPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.EnrollmentMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.EnrollmentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link EnrollmentPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentPersistenceAdapter implements EnrollmentPersistencePort {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public PagedResult<Enrollment> findByOrganizationId(
            Long organizationId, EnrollmentFilter filter, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationDate")));
                    Page<EnrollmentEntity> entityPage = enrollmentRepository.search(
                            organizationId,
                            filter == null ? null : filter.classId(),
                            filter == null ? null : filter.studentId(),
                            filter == null ? null : filter.status(),
                            pageRequest);
                    List<Enrollment> items = enrollmentMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated enrollments");
    }

    @Override
    public Optional<Enrollment> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> enrollmentRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(enrollmentMapper::toDomain),
                "Error fetching enrollment by id");
    }

    @Override
    public List<Enrollment> findByStudentIdAndOrganizationId(Long studentId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> enrollmentMapper.toDomain(
                        enrollmentRepository.findByStudentIdAndOrganizationId(studentId, organizationId)),
                "Error fetching enrollments by student");
    }

    @Override
    public boolean existsActiveByStudentIdAndClassId(Long studentId, Long classId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> enrollmentRepository.existsByStudentIdAndClassIdAndOrganizationIdAndStatus(
                        studentId, classId, organizationId, EnrollmentStatus.ACTIVE),
                "Error checking active enrollment");
    }

    @Override
    @Transactional
    public Enrollment save(Enrollment enrollment) {
        try {
            EnrollmentEntity saved = enrollmentRepository.saveAndFlush(enrollmentMapper.toEntity(enrollment));
            return enrollmentMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            if (hasConstraint(ex, "uq_enrollment_active")) {
                throw new DuplicateActiveEnrollmentException(enrollment.getStudentId(), enrollment.getClassId());
            }
            throw new DataBaseException("Error saving enrollment", ex);
        }
    }

    private boolean hasConstraint(Throwable throwable, String constraintName) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && constraintName.equals(violation.getConstraintName())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
