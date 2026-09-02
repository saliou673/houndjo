package com.houndjo.domain.models.query;

import java.util.List;

/**
 * Generic wrapper for a single page of query results.
 *
 * @param <T>        the item type
 * @param items      the items on the current page
 * @param totalItems total number of items matching the query
 * @param page       zero-based current page index
 * @param size       maximum items per page
 * @param totalPages total number of pages
 */
public record PagedResult<T>(List<T> items, long totalItems, int page, int size, int totalPages) {}
