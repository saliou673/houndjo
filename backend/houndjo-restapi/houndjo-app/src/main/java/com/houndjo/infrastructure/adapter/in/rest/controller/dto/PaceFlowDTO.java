package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import com.houndjo.domain.enumerations.PaceUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Target amount per session for one Quran tracking flow (Sabak, Sabqi or Dhor).
 *
 * @param unit   the unit the amount is expressed in
 * @param amount the target amount per session, in {@code unit}
 */
@Schema(name = "PaceFlow")
public record PaceFlowDTO(PaceUnit unit, BigDecimal amount) {}
