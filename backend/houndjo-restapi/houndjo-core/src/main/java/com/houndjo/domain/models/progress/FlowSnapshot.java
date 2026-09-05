package com.houndjo.domain.models.progress;

import java.time.Instant;

/**
 * The most recent validated portion recorded for a flow (Sabak or Sabqi), as of a given date.
 *
 * @param portion the last validated portion recorded
 * @param date    when that portion was recorded
 */
public record FlowSnapshot(QuranPortionRef portion, Instant date) {}
