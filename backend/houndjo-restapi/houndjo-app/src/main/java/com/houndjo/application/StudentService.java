package com.houndjo.application;

import com.houndjo.application.tenant.TenantContext;
import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.exceptions.StudentNotFoundException;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.models.student.StudentFilter;
import com.houndjo.domain.ports.in.StudentUseCase;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing {@link StudentUseCase}: CRUD for students, scoped to the
 * active organization resolved from {@link TenantContext}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService implements StudentUseCase {

    private final StudentPersistencePort studentPersistencePort;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Student> findAll(StudentFilter filter, int page, int size) {
        return studentPersistencePort.findByOrganizationId(
                tenantContext.requireCurrentOrganizationId(), filter, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Student getById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    public Student create(
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone) {
        Long organizationId = tenantContext.requireCurrentOrganizationId();
        log.debug("Creating student: organizationId={} firstName={} lastName={}", organizationId, firstName, lastName);
        Student student =
                Student.create(organizationId, firstName, lastName, birthDate, gender, guardianName, guardianPhone);
        return studentPersistencePort.save(student);
    }

    @Override
    public Student update(
            Long id,
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone) {
        log.debug("Updating student id={}", id);
        Student student = getByIdOrThrow(id);
        student.update(firstName, lastName, birthDate, gender, guardianName, guardianPhone);
        return studentPersistencePort.save(student);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting student id={}", id);
        getByIdOrThrow(id);
        studentPersistencePort.deleteById(id);
    }

    private Student getByIdOrThrow(Long id) {
        return studentPersistencePort
                .findByIdAndOrganizationId(id, tenantContext.requireCurrentOrganizationId())
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}
