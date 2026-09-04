package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.out.persistenceport.CoursePersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.CourseEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.CourseMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.CourseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link CoursePersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class CoursePersistenceAdapter implements CoursePersistencePort {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    public PagedResult<Course> findByClassIdAndOrganizationId(Long classId, Long organizationId, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    Page<CourseEntity> entityPage = courseRepository.findByClassIdAndOrganizationId(
                            classId,
                            organizationId,
                            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate")));
                    List<Course> items = courseMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated courses");
    }

    @Override
    public Optional<Course> findByIdAndClassIdAndOrganizationId(Long id, Long classId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> courseRepository
                        .findByIdAndClassIdAndOrganizationId(id, classId, organizationId)
                        .map(courseMapper::toDomain),
                "Error fetching course by id");
    }

    @Override
    @Transactional
    public Course save(Course course) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> courseMapper.toDomain(courseRepository.save(courseMapper.toEntity(course))),
                "Error saving course");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdapterPersistenceUtils.executeDbOperation(() -> courseRepository.deleteById(id), "Error deleting course");
    }

    @Override
    public Map<Long, Long> countByClassIdsAndOrganizationId(Collection<Long> classIds, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> courseRepository.countByClassIdsAndOrganizationId(classIds, organizationId).stream()
                        .collect(Collectors.toUnmodifiableMap(
                                CourseRepository.ClassCourseCount::getClassId,
                                CourseRepository.ClassCourseCount::getCourseCount)),
                "Error counting courses by class");
    }

    @Override
    public List<Course> findAllByIdInAndClassIdAndOrganizationId(
            Collection<Long> ids, Long classId, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> courseMapper.toDomain(
                        courseRepository.findAllByIdInAndClassIdAndOrganizationId(ids, classId, organizationId)),
                "Error fetching courses by ids and class");
    }
}
