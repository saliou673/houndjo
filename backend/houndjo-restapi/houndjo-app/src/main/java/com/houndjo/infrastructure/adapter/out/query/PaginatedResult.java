package com.houndjo.infrastructure.adapter.out.query;

import com.houndjo.domain.models.query.PagedResult;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
/** Generic paginated HTTP response wrapper that maps domain {@link com.houndjo.domain.models.query.PagedResult} items to DTOs. */
public class PaginatedResult<T> {
    /**
     * Current page items mapped to the target DTO type.
     */
    @Nonnull
    private final List<T> items;
    /**
     * Zero-based page index.
     */
    private final int page;
    /**
     * Maximum items per page.
     */
    private final int size;
    /**
     * Total number of pages.
     */
    private final int totalPages;
    /**
     * Total number of matching items.
     */
    private final long totalItems;

    public <E> PaginatedResult(PagedResult<E> pagedResult, Function<E, T> toDTO) {
        this.items = pagedResult.items().stream().map(toDTO).toList();
        this.page = pagedResult.page();
        this.size = pagedResult.size();
        this.totalPages = pagedResult.totalPages();
        this.totalItems = pagedResult.totalItems();
    }

    /**
     * Necessary for the serialization.
     */
    protected PaginatedResult() {
        this.items = new ArrayList<>();
        this.page = 0;
        this.size = 0;
        this.totalPages = 0;
        this.totalItems = 0;
    }
}
