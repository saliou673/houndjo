package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.enumerations.EnrollmentStatus;
import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.models.enrollment.EnrollmentFilter;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.EnrollmentUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.EnrollmentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.EnrollmentDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateEnrollmentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateEnrollmentCoursesRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the active organization's enrollments (student ↔ class/course).
 */
@Validated
@RestController
@Tag(name = "Enrollment management")
@PreAuthorize("hasAuthority('enrollment:read')")
@RequestMapping(path = "/api/v1/enrollments", version = "1.0")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentUseCase enrollmentUseCase;
    private final EnrollmentDtoMapper enrollmentDtoMapper;

    @GetMapping
    public PaginatedResult<EnrollmentDTO> getEnrollments(
            @RequestParam(required = false) @Nullable Long classId,
            @RequestParam(required = false) @Nullable Long studentId,
            @RequestParam(required = false) @Nullable EnrollmentStatus status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<Enrollment> result = enrollmentUseCase.findAll(
                new EnrollmentFilter(classId, studentId, status), pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, enrollmentDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public EnrollmentDTO getEnrollmentById(@PathVariable Long id) {
        return enrollmentDtoMapper.toDTO(enrollmentUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('enrollment:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public EnrollmentDTO createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        Set<Long> courseIds = request.courseIds() == null ? Set.of() : request.courseIds();
        return enrollmentDtoMapper.toDTO(enrollmentUseCase.enroll(request.studentId(), request.classId(), courseIds));
    }

    @PatchMapping("/{id}/courses")
    @PreAuthorize("hasAuthority('enrollment:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public EnrollmentDTO updateEnrollmentCourses(
            @PathVariable Long id, @Valid @RequestBody UpdateEnrollmentCoursesRequest request) {
        if (request.addCourseIds() != null && !request.addCourseIds().isEmpty()) {
            enrollmentUseCase.addCourses(id, request.addCourseIds());
        }
        if (request.removeCourseIds() != null && !request.removeCourseIds().isEmpty()) {
            enrollmentUseCase.removeCourses(id, request.removeCourseIds());
        }
        return enrollmentDtoMapper.toDTO(enrollmentUseCase.getById(id));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAuthority('enrollment:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public EnrollmentDTO endEnrollment(@PathVariable Long id) {
        return enrollmentDtoMapper.toDTO(enrollmentUseCase.end(id));
    }
}
