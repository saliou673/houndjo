package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.models.student.StudentFilter;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.StudentEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.StudentMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.StudentRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * JPA adapter implementing {@link StudentPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class StudentPersistenceAdapter implements StudentPersistencePort {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    public List<Student> findByIdsAndOrganizationId(Collection<Long> ids, Long organizationId) {
        if (ids.isEmpty()) return List.of();
        return AdapterPersistenceUtils.executeDbOperation(
                () -> studentMapper.toDomain(studentRepository.findByIdInAndOrganizationId(ids, organizationId)),
                "Error fetching students by ids");
    }

    @Override
    public PagedResult<Student> findByOrganizationId(Long organizationId, StudentFilter filter, int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationDate")));
                    String search = filter == null ? null : filter.search();
                    Page<StudentEntity> entityPage = StringUtils.hasText(search)
                            ? studentRepository.searchByOrganizationId(organizationId, search, pageRequest)
                            : studentRepository.findByOrganizationId(organizationId, pageRequest);
                    List<Student> items = studentMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated students");
    }

    @Override
    public Optional<Student> findByIdAndOrganizationId(Long id, Long organizationId) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> studentRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .map(studentMapper::toDomain),
                "Error fetching student by id");
    }

    @Override
    @Transactional
    public Student save(Student student) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> studentMapper.toDomain(studentRepository.save(studentMapper.toEntity(student))),
                "Error saving student");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdapterPersistenceUtils.executeDbOperation(() -> studentRepository.deleteById(id), "Error deleting student");
    }
}
