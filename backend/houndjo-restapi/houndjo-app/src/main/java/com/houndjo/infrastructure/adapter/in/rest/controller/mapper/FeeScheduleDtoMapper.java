package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.billing.FeeSchedule;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.FeeScheduleDTO;
import org.springframework.stereotype.Component;

/**
 * Maps {@link FeeSchedule} to {@link FeeScheduleDTO}.
 */
@Component
public class FeeScheduleDtoMapper {

    public FeeScheduleDTO toDTO(FeeSchedule feeSchedule) {
        return new FeeScheduleDTO(
                feeSchedule.getId(),
                feeSchedule.getType(),
                feeSchedule.getAmount(),
                feeSchedule.getCurrencyCode(),
                feeSchedule.getLabel(),
                feeSchedule.isActive());
    }
}
