package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.session.SessionFilter;
import com.houndjo.domain.ports.in.SessionUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SessionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.SessionDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateSessionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.GenerateSessionsRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateSessionRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing a course's sessions within the active organization.
 */
@Validated
@RestController
@Tag(name = "Session management")
@PreAuthorize("hasAuthority('session:read')")
@RequestMapping(path = "/api/v1/courses/{courseId}/sessions", version = "1.0")
@RequiredArgsConstructor
public class SessionController {

    private final SessionUseCase sessionUseCase;
    private final SessionDtoMapper sessionDtoMapper;

    @GetMapping
    public PaginatedResult<SessionDTO> getSessions(
            @PathVariable Long courseId,
            @RequestParam(required = false) @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<Session> result = sessionUseCase.findAll(
                courseId, new SessionFilter(fromDate, toDate), pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, sessionDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public SessionDTO getSessionById(@PathVariable Long courseId, @PathVariable Long id) {
        return sessionDtoMapper.toDTO(sessionUseCase.getById(courseId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('session:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public SessionDTO createSession(@PathVariable Long courseId, @Valid @RequestBody CreateSessionRequest request) {
        return sessionDtoMapper.toDTO(sessionUseCase.create(
                courseId, request.sessionDate(), request.startTime(), request.endTime(), request.teacherUserId()));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('session:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public List<SessionDTO> generateSessions(
            @PathVariable Long courseId, @Valid @RequestBody GenerateSessionsRequest request) {
        return sessionUseCase.generate(courseId, request.fromDate(), request.toDate()).stream()
                .map(sessionDtoMapper::toDTO)
                .toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('session:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public SessionDTO updateSession(
            @PathVariable Long courseId, @PathVariable Long id, @Valid @RequestBody UpdateSessionRequest request) {
        return sessionDtoMapper.toDTO(sessionUseCase.update(
                courseId, id, request.sessionDate(), request.startTime(), request.endTime(), request.teacherUserId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('session:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public SessionDTO cancelSession(@PathVariable Long courseId, @PathVariable Long id) {
        return sessionDtoMapper.toDTO(sessionUseCase.cancel(courseId, id));
    }
}
