package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.academic.Course;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.CourseUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.CourseDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateCourseRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST controller for managing a class's courses within the active organization.
 */
@Validated
@RestController
@Tag(name = "Course management")
@PreAuthorize("hasAuthority('course:read')")
@RequestMapping(path = "/api/v1/classes/{classId}/courses", version = "1.0")
@RequiredArgsConstructor
public class CourseController {

    private final CourseUseCase courseUseCase;
    private final CourseDtoMapper courseDtoMapper;

    @GetMapping
    public PaginatedResult<CourseDTO> getCourses(
            @PathVariable Long classId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<Course> result =
                courseUseCase.findByClassId(classId, pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, courseDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public CourseDTO getCourseById(@PathVariable Long classId, @PathVariable Long id) {
        return courseDtoMapper.toDTO(courseUseCase.getById(classId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('course:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public CourseDTO createCourse(@PathVariable Long classId, @Valid @RequestBody CreateCourseRequest request) {
        return courseDtoMapper.toDTO(courseUseCase.create(
                classId,
                request.name(),
                request.description(),
                request.type(),
                request.quranMode(),
                request.quranScopeFromJuz(),
                request.quranScopeToJuz(),
                request.bookTitle(),
                request.bookTotalChapters(),
                request.bookTotalPages()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('course:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public CourseDTO updateCourse(
            @PathVariable Long classId, @PathVariable Long id, @Valid @RequestBody UpdateCourseRequest request) {
        return courseDtoMapper.toDTO(courseUseCase.update(
                classId,
                id,
                request.name(),
                request.description(),
                request.type(),
                request.quranMode(),
                request.quranScopeFromJuz(),
                request.quranScopeToJuz(),
                request.bookTitle(),
                request.bookTotalChapters(),
                request.bookTotalPages()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('course:delete') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public void deleteCourse(@PathVariable Long classId, @PathVariable Long id) {
        courseUseCase.delete(classId, id);
    }
}
