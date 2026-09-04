package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.exceptions.CourseNotInClassException;
import com.houndjo.domain.exceptions.DuplicateActiveEnrollmentException;
import com.houndjo.domain.exceptions.EnrollmentNotFoundException;
import com.houndjo.domain.exceptions.SchoolClassNotFoundException;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.models.enrollment.EnrollmentFilter;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.EnrollmentUseCase;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.EnrollmentPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.SchoolClassPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link EnrollmentUseCase}: enrolling students into a class and
 * its courses, scoped to the active organization resolved from {@link TenantContext}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentService implements EnrollmentUseCase {

    private final EnrollmentPersistencePort enrollmentPersistencePort;
    private final StudentPersistencePort studentPersistencePort;
    private final SchoolClassPersistencePort schoolClassPersistencePort;
    private final CoursePersistencePort coursePersistencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Enrollment> findAll(EnrollmentFilter filter, int page, int size) {
        return enrollmentPersistencePort.findByOrganizationId(
                tenantContext.requireCurrentOrganizationId(), filter, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment getById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getByStudent(Long studentId) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireStudent(studentId, organizationId);
        return enrollmentPersistencePort.findByStudentIdAndOrganizationId(studentId, organizationId);
    }

    @Override
    public Enrollment enroll(Long studentId, Long classId, Set<Long> courseIds) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireStudent(studentId, organizationId);
        requireClass(classId, organizationId);
        requireCoursesBelongToClass(courseIds, classId, organizationId);
        if (enrollmentPersistencePort.existsActiveByStudentIdAndClassId(studentId, classId, organizationId)) {
            throw new DuplicateActiveEnrollmentException(studentId, classId);
        }
        log.debug("Enrolling student: organizationId={} studentId={} classId={}", organizationId, studentId, classId);
        Enrollment enrollment = Enrollment.create(organizationId, studentId, classId, courseIds, LocalDate.now());
        return enrollmentPersistencePort.save(enrollment);
    }

    @Override
    public Enrollment end(Long id) {
        log.debug("Ending enrollment id={}", id);
        Enrollment enrollment = getByIdOrThrow(id);
        enrollment.end(LocalDate.now());
        return enrollmentPersistencePort.save(enrollment);
    }

    @Override
    public Enrollment addCourses(Long id, Set<Long> courseIds) {
        Enrollment enrollment = getByIdOrThrow(id);
        requireCoursesBelongToClass(courseIds, enrollment.getClassId(), enrollment.getOrganizationId());
        enrollment.addCourses(courseIds);
        return enrollmentPersistencePort.save(enrollment);
    }

    @Override
    public Enrollment removeCourses(Long id, Set<Long> courseIds) {
        Enrollment enrollment = getByIdOrThrow(id);
        enrollment.removeCourses(courseIds);
        return enrollmentPersistencePort.save(enrollment);
    }

    private Enrollment getByIdOrThrow(Long id) {
        return enrollmentPersistencePort
                .findByIdAndOrganizationId(id, tenantContext.requireCurrentOrganizationId())
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    private void requireStudent(Long studentId, Long organizationId) {
        studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private void requireClass(Long classId, Long organizationId) {
        schoolClassPersistencePort
                .findByIdAndOrganizationId(classId, organizationId)
                .orElseThrow(() -> new SchoolClassNotFoundException(classId));
    }

    private void requireCoursesBelongToClass(Set<Long> courseIds, Long classId, Long organizationId) {
        if (courseIds == null || courseIds.isEmpty()) {
            return;
        }
        long matching = coursePersistencePort
                .findAllByIdInAndClassIdAndOrganizationId(courseIds, classId, organizationId)
                .size();
        if (matching != courseIds.size()) {
            throw new CourseNotInClassException(classId);
        }
    }
}
