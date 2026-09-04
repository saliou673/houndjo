package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.models.student.StudentFilter;
import com.houndjo.domain.ports.in.StudentUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.StudentDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateStudentRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the active organization's students.
 */
@Validated
@RestController
@Tag(name = "Student management")
@PreAuthorize("hasAuthority('student:read')")
@RequestMapping(path = "/api/v1/students", version = "1.0")
@RequiredArgsConstructor
public class StudentController {

    private final StudentUseCase studentUseCase;
    private final StudentDtoMapper studentDtoMapper;

    @GetMapping
    public PaginatedResult<StudentDTO> getStudents(
            @RequestParam(required = false) @Nullable String search,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<Student> result =
                studentUseCase.findAll(new StudentFilter(search), pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, studentDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public StudentDTO getStudentById(@PathVariable Long id) {
        return studentDtoMapper.toDTO(studentUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('student:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public StudentDTO createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return studentDtoMapper.toDTO(studentUseCase.create(
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.gender(),
                request.guardianName(),
                request.guardianPhone()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('student:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public StudentDTO updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return studentDtoMapper.toDTO(studentUseCase.update(
                id,
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.gender(),
                request.guardianName(),
                request.guardianPhone()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('student:delete') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public void deleteStudent(@PathVariable Long id) {
        studentUseCase.delete(id);
    }
}
