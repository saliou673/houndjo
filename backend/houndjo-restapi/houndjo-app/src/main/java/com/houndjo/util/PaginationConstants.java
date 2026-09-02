package com.houndjo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
/** Pagination defaults shared across query services. */
public final class PaginationConstants {
    /**
     * Default number of items returned per page.
     */
    public static final int DEFAULT_PAGE_SIZE_INT = 50;
}
