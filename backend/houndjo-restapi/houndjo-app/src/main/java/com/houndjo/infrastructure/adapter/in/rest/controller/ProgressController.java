package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.models.progress.ProgressFilter;
import com.houndjo.domain.models.progress.ProgressRecord;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.ProgressUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.ProgressDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RecordProgressRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateProgressRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
 * REST controller for recording and reviewing progress within the active organization.
 */
@Validated
@RestController
@Tag(name = "Progress tracking")
@PreAuthorize("hasAuthority('progress:read')")
@RequestMapping(path = "/api/v1/progress", version = "1.0")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressUseCase progressUseCase;
    private final ProgressDtoMapper progressDtoMapper;

    @GetMapping
    public PaginatedResult<ProgressDTO> getProgressRecords(
            @RequestParam(required = false) @Nullable Long studentId,
            @RequestParam(required = false) @Nullable Long courseId,
            @RequestParam(required = false) @Nullable ProgressFlow flow,
            @RequestParam(required = false) @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<ProgressRecord> result = progressUseCase.findAll(
                new ProgressFilter(studentId, courseId, flow, fromDate, toDate),
                pageable.getPageNumber(),
                pageable.getPageSize());
        return new PaginatedResult<>(result, progressDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public ProgressDTO getProgressRecordById(@PathVariable Long id) {
        return progressDtoMapper.toDTO(progressUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('progress:create') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public ProgressDTO recordProgress(@Valid @RequestBody RecordProgressRequest request) {
        return progressDtoMapper.toDTO(progressUseCase.record(
                request.studentId(),
                request.courseId(),
                request.sessionId(),
                request.flow(),
                request.fromSurah(),
                request.fromVerse(),
                request.toSurah(),
                request.toVerse(),
                request.lessonId(),
                request.chapterNo(),
                request.pageNo(),
                request.errorCount(),
                request.fluency(),
                request.tajweed(),
                request.status(),
                request.note()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('progress:update') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public ProgressDTO updateProgress(@PathVariable Long id, @Valid @RequestBody UpdateProgressRequest request) {
        return progressDtoMapper.toDTO(progressUseCase.update(
                id,
                request.fromSurah(),
                request.fromVerse(),
                request.toSurah(),
                request.toVerse(),
                request.lessonId(),
                request.chapterNo(),
                request.pageNo(),
                request.errorCount(),
                request.fluency(),
                request.tajweed(),
                request.status(),
                request.note()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('progress:delete') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN', 'TEACHER')")
    public void deleteProgress(@PathVariable Long id) {
        progressUseCase.delete(id);
    }
}
