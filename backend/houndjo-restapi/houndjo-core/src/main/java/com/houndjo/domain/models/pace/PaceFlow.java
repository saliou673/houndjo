package com.houndjo.domain.models.pace;

import com.houndjo.domain.enumerations.PaceUnit;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * The target amount per session for one Quran tracking flow (Sabak, Sabqi or Dhor).
 *
 * @param unit   the unit the amount is expressed in
 * @param amount the target amount per session, in {@code unit}
 */
public record PaceFlow(PaceUnit unit, BigDecimal amount) {

    public PaceFlow {
        Objects.requireNonNull(unit, "unit must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }
}
