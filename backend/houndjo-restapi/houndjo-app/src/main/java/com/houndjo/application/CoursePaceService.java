package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.enumerations.QuranFlow;
import com.houndjo.domain.exceptions.CourseNotFoundException;
import com.houndjo.domain.exceptions.CoursePaceNotFoundException;
import com.houndjo.domain.exceptions.InvalidCoursePaceConfigException;
import com.houndjo.domain.exceptions.PaceNotApplicableException;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.academic.QuranTrackingConfig;
import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.models.pace.PaceFlow;
import com.houndjo.domain.models.pace.PortionCalculator;
import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.ports.in.CoursePaceUseCase;
import com.houndjo.domain.ports.out.persistenceport.CoursePacePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link CoursePaceUseCase}: pace configuration and portion
 * computation for courses of the active organization.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CoursePaceService implements CoursePaceUseCase {

    private final CoursePacePersistencePort coursePacePersistencePort;
    private final CoursePersistencePort coursePersistencePort;
    private final StudentPersistencePort studentPersistencePort;
    private final QuranReferencePort quranReferencePort;
    private final TenantContext tenantContext;

    @Override
    public CoursePace setPace(
            Long courseId,
            PaceUnit unit,
            BigDecimal amountPerSession,
            int sessionsPerWeek,
            PaceFlow sabak,
            PaceFlow sabqi,
            PaceFlow dhor,
            Integer dhorCycleDays) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        Course course = requireCourse(courseId, organizationId);
        if (course.getType() == CourseType.QURAN) {
            if (sabak == null || sabqi == null || dhor == null || dhorCycleDays == null) {
                throw new InvalidCoursePaceConfigException(
                        "QURAN courses require sabak, sabqi, dhor and dhorCycleDays");
            }
        } else {
            sabak = null;
            sabqi = null;
            dhor = null;
            dhorCycleDays = null;
        }
        log.debug("Setting pace: organizationId={} courseId={}", organizationId, courseId);
        CoursePace existing = coursePacePersistencePort
                .findByCourseIdAndOrganizationId(courseId, organizationId)
                .orElse(null);
        CoursePace coursePace;
        if (existing != null) {
            existing.update(unit, amountPerSession, sessionsPerWeek, sabak, sabqi, dhor, dhorCycleDays);
            coursePace = existing;
        } else {
            coursePace = CoursePace.create(
                    organizationId,
                    courseId,
                    unit,
                    amountPerSession,
                    sessionsPerWeek,
                    sabak,
                    sabqi,
                    dhor,
                    dhorCycleDays);
        }
        return coursePacePersistencePort.save(coursePace);
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePace getPace(Long courseId) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireCourse(courseId, organizationId);
        return coursePacePersistencePort
                .findByCourseIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CoursePaceNotFoundException(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public QuranPortion getNextPortion(Long courseId, Long studentId, QuranFlow flow) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        Course course = requireCourse(courseId, organizationId);
        if (course.getType() != CourseType.QURAN) {
            throw new PaceNotApplicableException(courseId);
        }
        studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        CoursePace pace = coursePacePersistencePort
                .findByCourseIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CoursePaceNotFoundException(courseId));
        PaceFlow flowPace =
                switch (flow) {
                    case SABAK -> pace.getSabak();
                    case SABQI -> pace.getSabqi();
                    case DHOR -> pace.getDhor();
                };
        QuranTrackingConfig trackingConfig = (QuranTrackingConfig) course.getTrackingConfig();
        return new PortionCalculator(quranReferencePort)
                .computeNextPortionFromScopeStart(trackingConfig.fromJuz(), flowPace);
    }

    private Course requireCourse(Long courseId, Long organizationId) {
        return coursePersistencePort
                .findByIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }
}
