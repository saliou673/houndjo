package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.exceptions.CourseNotFoundException;
import com.houndjo.domain.exceptions.CoursePaceNotFoundException;
import com.houndjo.domain.exceptions.OrganizationNotFoundException;
import com.houndjo.domain.exceptions.PaceNotApplicableException;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.academic.QuranTrackingConfig;
import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.progress.ProgressState;
import com.houndjo.domain.models.progress.ProgressStateCalculator;
import com.houndjo.domain.models.progress.RevisionAlert;
import com.houndjo.domain.ports.in.ProgressStateUseCase;
import com.houndjo.domain.ports.out.persistenceport.CoursePacePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.ProgressPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link ProgressStateUseCase}: composes {@link ProgressRecord}
 * history with the course's Quran scope and pace into a {@link ProgressStateCalculator}
 * computation.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProgressStateService implements ProgressStateUseCase {

    private final ProgressPersistencePort progressPersistencePort;
    private final StudentPersistencePort studentPersistencePort;
    private final CoursePersistencePort coursePersistencePort;
    private final CoursePacePersistencePort coursePacePersistencePort;
    private final QuranReferencePort quranReferencePort;
    private final TenantContext tenantContext;

    @Override
    public ProgressState getProgressState(Long studentId, Long courseId) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        Course course = requireQuranCourse(courseId, organizationId);
        CoursePace pace = coursePacePersistencePort
                .findByCourseIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CoursePaceNotFoundException(courseId));
        QuranTrackingConfig trackingConfig = (QuranTrackingConfig) course.getTrackingConfig();
        List<ProgressRecord> records = progressPersistencePort.findByStudentIdAndCourseIdAndOrganizationId(
                studentId, courseId, organizationId);
        return new ProgressStateCalculator(quranReferencePort)
                .compute(
                        records,
                        trackingConfig.fromJuz(),
                        trackingConfig.toJuz(),
                        pace.getDhorCycleDays(),
                        Instant.now());
    }

    @Override
    public List<RevisionAlert> getRevisionAlerts(Long organizationId) {
        if (!tenantContext.requireCurrentOrganizationId().equals(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
        List<ProgressRecord> allRecords = progressPersistencePort.findAllByOrganizationId(organizationId);
        Map<StudentCourseKey, List<ProgressRecord>> byStudentAndCourse = allRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> new StudentCourseKey(record.getStudentId(), record.getCourseId())));

        Instant now = Instant.now();
        return byStudentAndCourse.entrySet().stream()
                .map(entry -> toRevisionAlert(entry.getKey(), entry.getValue(), organizationId, now))
                .filter(alert -> alert != null && !alert.stalePortions().isEmpty())
                .toList();
    }

    private RevisionAlert toRevisionAlert(
            StudentCourseKey key, List<ProgressRecord> records, Long organizationId, Instant now) {
        Course course = coursePersistencePort
                .findByIdAndOrganizationId(key.courseId(), organizationId)
                .orElse(null);
        if (course == null || course.getType() != CourseType.QURAN) {
            return null;
        }
        CoursePace pace = coursePacePersistencePort
                .findByCourseIdAndOrganizationId(key.courseId(), organizationId)
                .orElse(null);
        if (pace == null || pace.getDhorCycleDays() == null) {
            return null;
        }
        QuranTrackingConfig trackingConfig = (QuranTrackingConfig) course.getTrackingConfig();
        ProgressState state = new ProgressStateCalculator(quranReferencePort)
                .compute(records, trackingConfig.fromJuz(), trackingConfig.toJuz(), pace.getDhorCycleDays(), now);
        return new RevisionAlert(key.studentId(), key.courseId(), state.stalePortions());
    }

    private Course requireQuranCourse(Long courseId, Long organizationId) {
        Course course = coursePersistencePort
                .findByIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        if (course.getType() != CourseType.QURAN) {
            throw new PaceNotApplicableException(courseId);
        }
        return course;
    }

    private record StudentCourseKey(Long studentId, Long courseId) {}
}
