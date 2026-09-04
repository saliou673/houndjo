package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.domain.exceptions.CourseNotFoundException;
import com.houndjo.domain.exceptions.InvalidCourseConfigException;
import com.houndjo.domain.exceptions.SchoolClassNotFoundException;
import com.houndjo.domain.models.academic.BookTrackingConfig;
import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.academic.QaidaTrackingConfig;
import com.houndjo.domain.models.academic.QuranTrackingConfig;
import com.houndjo.domain.models.academic.TrackingConfig;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.CourseUseCase;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.domain.ports.out.persistenceport.SchoolClassPersistencePort;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link CourseUseCase}: CRUD for courses, scoped to the active
 * organization and their owning class.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CourseService implements CourseUseCase {

    private final CoursePersistencePort coursePersistencePort;
    private final SchoolClassPersistencePort schoolClassPersistencePort;
    private final QuranReferencePort quranReferencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Course> findByClassId(Long classId, int page, int size) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireClass(classId, organizationId);
        return coursePersistencePort.findByClassIdAndOrganizationId(classId, organizationId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getById(Long classId, Long id) {
        return getByIdOrThrow(classId, id);
    }

    @Override
    public Course create(
            Long classId,
            String name,
            String description,
            CourseType type,
            List<String> qaidaLessons,
            QuranMode quranMode,
            Integer quranScopeFromJuz,
            Integer quranScopeToJuz,
            String bookTitle,
            Integer bookTotalChapters,
            Integer bookTotalPages) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireClass(classId, organizationId);
        TrackingConfig trackingConfig = buildTrackingConfig(
                type,
                qaidaLessons,
                quranMode,
                quranScopeFromJuz,
                quranScopeToJuz,
                bookTitle,
                bookTotalChapters,
                bookTotalPages);
        log.debug("Creating course: organizationId={} classId={} name={} type={}", organizationId, classId, name, type);
        Course course = Course.create(organizationId, classId, name, description, trackingConfig);
        return coursePersistencePort.save(course);
    }

    @Override
    public Course update(
            Long classId,
            Long id,
            String name,
            String description,
            CourseType type,
            List<String> qaidaLessons,
            QuranMode quranMode,
            Integer quranScopeFromJuz,
            Integer quranScopeToJuz,
            String bookTitle,
            Integer bookTotalChapters,
            Integer bookTotalPages) {
        log.debug("Updating course id={}", id);
        Course course = getByIdOrThrow(classId, id);
        TrackingConfig trackingConfig = buildTrackingConfig(
                type,
                qaidaLessons,
                quranMode,
                quranScopeFromJuz,
                quranScopeToJuz,
                bookTitle,
                bookTotalChapters,
                bookTotalPages);
        course.update(name, description, trackingConfig);
        return coursePersistencePort.save(course);
    }

    @Override
    public void delete(Long classId, Long id) {
        log.debug("Deleting course id={}", id);
        getByIdOrThrow(classId, id);
        coursePersistencePort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> countByClassIds(List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Map.of();
        }
        return coursePersistencePort
                .countByClassIdsAndOrganizationId(classIds, tenantContext.requireCurrentOrganizationId())
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Math.toIntExact(entry.getValue())));
    }

    private Course getByIdOrThrow(Long classId, Long id) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        requireClass(classId, organizationId);
        return coursePersistencePort
                .findByIdAndClassIdAndOrganizationId(id, classId, organizationId)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    private void requireClass(Long classId, Long organizationId) {
        schoolClassPersistencePort
                .findByIdAndOrganizationId(classId, organizationId)
                .orElseThrow(() -> new SchoolClassNotFoundException(classId));
    }

    private TrackingConfig buildTrackingConfig(
            CourseType type,
            List<String> qaidaLessons,
            QuranMode quranMode,
            Integer quranScopeFromJuz,
            Integer quranScopeToJuz,
            String bookTitle,
            Integer bookTotalChapters,
            Integer bookTotalPages) {
        return switch (type) {
            case QURAN -> {
                if (quranMode == null || quranScopeFromJuz == null || quranScopeToJuz == null) {
                    throw new InvalidCourseConfigException(
                            CourseType.QURAN, "quranMode, quranScopeFromJuz, quranScopeToJuz");
                }
                quranReferencePort.portionForJuz(quranScopeFromJuz);
                quranReferencePort.portionForJuz(quranScopeToJuz);
                yield new QuranTrackingConfig(quranMode, quranScopeFromJuz, quranScopeToJuz);
            }
            case QAIDA -> {
                if (qaidaLessons == null || qaidaLessons.isEmpty()) {
                    throw new InvalidCourseConfigException(CourseType.QAIDA, "qaidaLessons");
                }
                yield new QaidaTrackingConfig(qaidaLessons);
            }
            case BOOK -> {
                if (bookTitle == null || bookTitle.isBlank()) {
                    throw new InvalidCourseConfigException(CourseType.BOOK, "bookTitle");
                }
                yield new BookTrackingConfig(bookTitle, bookTotalChapters, bookTotalPages);
            }
        };
    }
}
