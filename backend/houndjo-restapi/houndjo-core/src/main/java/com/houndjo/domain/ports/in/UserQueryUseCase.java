package com.houndjo.domain.ports.in;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserFilter;

/**
 * Read-only query use case for users.
 */
public interface UserQueryUseCase {

    /**
     * Returns a page of users matching the given filter.
     *
     * @param filter criteria to apply (null fields mean no constraint)
     * @param page   zero-based page index
     * @param size   maximum number of items per page
     * @return a page of matching users
     */
    PagedResult<User> findAll(UserFilter filter, int page, int size);

    /**
     * Counts users matching the given filter.
     *
     * @param filter criteria to apply
     * @return number of matching users
     */
    long count(UserFilter filter);
}
