package com.houndjo.domain.models.session;

import java.time.LocalDate;

/**
 * Filter for listing sessions of a course. Null fields mean "no constraint".
 *
 * @param fromDate earliest session date, inclusive
 * @param toDate   latest session date, inclusive
 */
public record SessionFilter(LocalDate fromDate, LocalDate toDate) {}
