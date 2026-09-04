package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.academic.SchoolClass;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.SchoolClassUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.ClassDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateClassRequest;
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
 * REST controller for managing the active organization's classes (grade/class levels).
 */
@Validated
@RestController
@Tag(name = "Class management")
@PreAuthorize("hasAuthority('class:read')")
@RequestMapping(path = "/api/v1/classes", version = "1.0")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassUseCase schoolClassUseCase;
    private final ClassDtoMapper classDtoMapper;

    @GetMapping
    public PaginatedResult<ClassDTO> getClasses(
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<SchoolClass> result = schoolClassUseCase.findAll(pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, classDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public ClassDTO getClassById(@PathVariable Long id) {
        return classDtoMapper.toDTO(schoolClassUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('class:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public ClassDTO createClass(@Valid @RequestBody CreateClassRequest request) {
        return classDtoMapper.toDTO(schoolClassUseCase.create(
                request.name(), request.description(), request.displayOrder() == null ? 0 : request.displayOrder()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('class:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public ClassDTO updateClass(@PathVariable Long id, @Valid @RequestBody UpdateClassRequest request) {
        return classDtoMapper.toDTO(schoolClassUseCase.update(
                id, request.name(), request.description(), request.displayOrder() == null ? 0 : request.displayOrder()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('class:delete') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public void deleteClass(@PathVariable Long id) {
        schoolClassUseCase.delete(id);
    }
}
