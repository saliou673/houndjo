package com.houndjo.domain.models.student;

/**
 * Filter for listing students within the active organization. Only name search is supported;
 * filtering by class needs the Enrollment aggregate (not yet modeled) and will be added once it
 * lands.
 *
 * @param search optional case-insensitive substring match against first or last name
 */
public record StudentFilter(String search) {}
