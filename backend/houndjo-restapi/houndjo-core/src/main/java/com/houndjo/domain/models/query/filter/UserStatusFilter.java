package com.houndjo.domain.models.query.filter;

import com.houndjo.domain.enumerations.UserStatus;
import java.io.Serial;
import lombok.NoArgsConstructor;

/**
 * Filter class for {@link UserStatus} enum attributes.
 */
@NoArgsConstructor
public class UserStatusFilter extends EnumFilter<UserStatus> {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserStatusFilter(UserStatusFilter filter) {
        super(filter);
    }

    @Override
    public UserStatusFilter copy() {
        return new UserStatusFilter(this);
    }
}
