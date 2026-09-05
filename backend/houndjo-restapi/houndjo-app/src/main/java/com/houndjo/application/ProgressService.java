package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.exceptions.CourseNotFoundException;
import com.houndjo.domain.exceptions.InvalidProgressPortionException;
import com.houndjo.domain.exceptions.ProgressRecordNotFoundException;
import com.houndjo.domain.exceptions.SessionNotFoundException;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.academic.BookTrackingConfig;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.academic.QaidaTrackingConfig;
import com.houndjo.domain.models.progress.ChapterPortionRef;
import com.houndjo.domain.models.progress.LessonPortionRef;
import com.houndjo.domain.models.progress.PortionRef;
import com.houndjo.domain.models.progress.ProgressFilter;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.progress.QuranPortionRef;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.ProgressUseCase;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.ProgressPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link ProgressUseCase}: per-session, per-student progress
 * recording, scoped to the active organization and validated against its students, courses,
 * sessions and (for Quran flows) the Quran reference data (E2).
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProgressService implements ProgressUseCase {

    private final ProgressPersistencePort progressPersistencePort;
    private final StudentPersistencePort studentPersistencePort;
    private final CoursePersistencePort coursePersistencePort;
    private final SessionPersistencePort sessionPersistencePort;
    private final QuranReferencePort quranReferencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ProgressRecord> findAll(ProgressFilter filter, int page, int size) {
        return progressPersistencePort.findByOrganizationId(
                tenantContext.requireCurrentOrganizationId(), filter, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressRecord getById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    public ProgressRecord record(
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            Integer fromSurah,
            Integer fromVerse,
            Integer toSurah,
            Integer toVerse,
            Long lessonId,
            Integer chapterNo,
            Integer pageNo,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireStudent(studentId, organizationId);
        Course course = requireCourse(courseId, organizationId);
        requireSession(sessionId, courseId, organizationId);
        PortionRef portion = buildPortion(flow, fromSurah, fromVerse, toSurah, toVerse, lessonId, chapterNo, pageNo);
        validatePortion(course, flow, portion);
        log.debug(
                "Recording progress: organizationId={} studentId={} courseId={} sessionId={} flow={}",
                organizationId,
                studentId,
                courseId,
                sessionId,
                flow);
        ProgressRecord progressRecord = ProgressRecord.create(
                organizationId,
                studentId,
                courseId,
                sessionId,
                flow,
                portion,
                errorCount,
                fluency,
                tajweed,
                status,
                note);
        return progressPersistencePort.save(progressRecord);
    }

    @Override
    public ProgressRecord update(
            Long id,
            Integer fromSurah,
            Integer fromVerse,
            Integer toSurah,
            Integer toVerse,
            Long lessonId,
            Integer chapterNo,
            Integer pageNo,
            int errorCount,
            FluencyRating fluency,
            FluencyRating tajweed,
            ProgressStatus status,
            String note) {
        log.debug("Updating progress record id={}", id);
        ProgressRecord progressRecord = getByIdOrThrow(id);
        PortionRef portion = buildPortion(
                progressRecord.getFlow(), fromSurah, fromVerse, toSurah, toVerse, lessonId, chapterNo, pageNo);
        validatePortion(
                requireCourse(progressRecord.getCourseId(), progressRecord.getOrganizationId()),
                progressRecord.getFlow(),
                portion);
        progressRecord.update(portion, errorCount, fluency, tajweed, status, note);
        return progressPersistencePort.save(progressRecord);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting progress record id={}", id);
        getByIdOrThrow(id);
        progressPersistencePort.deleteById(id);
    }

    private ProgressRecord getByIdOrThrow(Long id) {
        return progressPersistencePort
                .findByIdAndOrganizationId(id, tenantContext.requireCurrentOrganizationId())
                .orElseThrow(() -> new ProgressRecordNotFoundException(id));
    }

    private void requireStudent(Long studentId, Long organizationId) {
        studentPersistencePort
                .findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private Course requireCourse(Long courseId, Long organizationId) {
        return coursePersistencePort
                .findByIdAndOrganizationId(courseId, organizationId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private void requireSession(Long sessionId, Long courseId, Long organizationId) {
        sessionPersistencePort
                .findByIdAndCourseIdAndOrganizationId(sessionId, courseId, organizationId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    private void validatePortion(Course course, ProgressFlow flow, PortionRef portion) {
        boolean matchingType =
                switch (course.getType()) {
                    case QURAN -> portion instanceof QuranPortionRef;
                    case QAIDA -> portion instanceof LessonPortionRef;
                    case BOOK -> portion instanceof ChapterPortionRef;
                };
        if (!matchingType) {
            throw new InvalidProgressPortionException(flow);
        }
        switch (portion) {
            case QuranPortionRef quran -> {
                if (quran.fromSurah() > quran.toSurah()
                        || (quran.fromSurah() == quran.toSurah() && quran.fromVerse() > quran.toVerse())) {
                    throw new InvalidProgressPortionException(flow);
                }
            }
            case LessonPortionRef lesson -> {
                QaidaTrackingConfig config = (QaidaTrackingConfig) course.getTrackingConfig();
                if (lesson.lessonId() < 1
                        || lesson.lessonId() > config.lessons().size()) {
                    throw new InvalidProgressPortionException(flow);
                }
            }
            case ChapterPortionRef chapter -> {
                BookTrackingConfig config = (BookTrackingConfig) course.getTrackingConfig();
                if (chapter.chapterNo() < 1
                        || chapter.chapterNo() > Short.MAX_VALUE
                        || chapter.pageNo() < 1
                        || chapter.pageNo() > Short.MAX_VALUE
                        || (config.totalChapters() != null && chapter.chapterNo() > config.totalChapters())
                        || (config.totalPages() != null && chapter.pageNo() > config.totalPages())) {
                    throw new InvalidProgressPortionException(flow);
                }
            }
        }
    }

    private PortionRef buildPortion(
            ProgressFlow flow,
            Integer fromSurah,
            Integer fromVerse,
            Integer toSurah,
            Integer toVerse,
            Long lessonId,
            Integer chapterNo,
            Integer pageNo) {
        return switch (flow) {
            case SABAK, SABQI, DHOR -> {
                if (fromSurah == null || fromVerse == null || toSurah == null || toVerse == null) {
                    throw new InvalidProgressPortionException(flow);
                }
                quranReferencePort.pageOf(fromSurah, fromVerse);
                quranReferencePort.pageOf(toSurah, toVerse);
                yield new QuranPortionRef(fromSurah, fromVerse, toSurah, toVerse);
            }
            case LESSON -> {
                if (lessonId == null) {
                    throw new InvalidProgressPortionException(flow);
                }
                yield new LessonPortionRef(lessonId);
            }
            case CHAPTER -> {
                if (chapterNo == null || pageNo == null) {
                    throw new InvalidProgressPortionException(flow);
                }
                yield new ChapterPortionRef(chapterNo, pageNo);
            }
        };
    }
}
