package com.houndjo.infrastructure.adapter.in.rest.controller;

import static com.houndjo.util.PaginationConstants.DEFAULT_PAGE_SIZE_INT;

import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.ports.in.FeeScheduleUseCase;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.FeeScheduleDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.FeeScheduleDtoMapper;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateFeeScheduleRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateFeeScheduleRequest;
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
 * REST controller for managing the active organization's fee schedules (registration/tuition).
 */
@Validated
@RestController
@Tag(name = "Fee schedule management")
@PreAuthorize("hasAuthority('billing:manage') and @authz.hasOrgRole('SCHOOL_OWNER', 'SCHOOL_ADMIN')")
@RequestMapping(path = "/api/v1/fee-schedules", version = "1.0")
@RequiredArgsConstructor
public class FeeScheduleController {

    private final FeeScheduleUseCase feeScheduleUseCase;
    private final FeeScheduleDtoMapper feeScheduleDtoMapper;

    @GetMapping
    public PaginatedResult<FeeScheduleDTO> getFeeSchedules(
            @PageableDefault(size = DEFAULT_PAGE_SIZE_INT, sort = "creationDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PagedResult<FeeSchedule> result =
                feeScheduleUseCase.findAll(pageable.getPageNumber(), pageable.getPageSize());
        return new PaginatedResult<>(result, feeScheduleDtoMapper::toDTO);
    }

    @GetMapping("/{id}")
    public FeeScheduleDTO getFeeScheduleById(@PathVariable Long id) {
        return feeScheduleDtoMapper.toDTO(feeScheduleUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeeScheduleDTO createFeeSchedule(@Valid @RequestBody CreateFeeScheduleRequest request) {
        return feeScheduleDtoMapper.toDTO(
                feeScheduleUseCase.create(request.type(), request.amount(), request.currencyCode(), request.label()));
    }

    @PutMapping("/{id}")
    public FeeScheduleDTO updateFeeSchedule(@PathVariable Long id, @Valid @RequestBody UpdateFeeScheduleRequest request) {
        FeeSchedule updated = feeScheduleUseCase.update(
                id, request.type(), request.amount(), request.currencyCode(), request.label(), request.active());
        return feeScheduleDtoMapper.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeeSchedule(@PathVariable Long id) {
        feeScheduleUseCase.delete(id);
    }
}
