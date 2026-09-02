package com.houndjo.domain.models.user;

import com.houndjo.domain.models.query.AuditableFilter;
import com.houndjo.domain.models.query.filter.LongFilter;
import com.houndjo.domain.models.query.filter.StringFilter;
import com.houndjo.domain.models.query.filter.UserGenderFilter;
import com.houndjo.domain.models.query.filter.UserStatusFilter;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;

/**
 * Domain-level filter for querying users.
 * Null fields mean "no constraint". Fields support the full operator set
 * (equals, notEquals, contains, in, notIn, specified, range, etc.) from their Filter type.
 * <p>
 * {@code @Setter} is required so Spring MVC can bind query parameters like
 * {@code ?email.contains=foo&status.equals=ACTIVE} directly to this object
 * via {@code @ModelAttribute}.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public final class UserFilter extends AuditableFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;
    private StringFilter email;
    private StringFilter firstName;
    private StringFilter lastName;
    private UserGenderFilter gender;
    private UserStatusFilter status;
    private StringFilter phoneNumber;
    private StringFilter address;
    private StringFilter languageKey;
    private StringFilter roleGroupName;

    public UserFilter(UserFilter other) {
        this.id = other.id == null ? null : other.id.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.firstName = other.firstName == null ? null : other.firstName.copy();
        this.lastName = other.lastName == null ? null : other.lastName.copy();
        this.gender = other.gender == null ? null : other.gender.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.phoneNumber = other.phoneNumber == null ? null : other.phoneNumber.copy();
        this.address = other.address == null ? null : other.address.copy();
        this.languageKey = other.languageKey == null ? null : other.languageKey.copy();
        this.roleGroupName = other.roleGroupName == null ? null : other.roleGroupName.copy();
        this.setCreationDate(
                other.getCreationDate() == null ? null : other.getCreationDate().copy());
        this.setLastUpdateDate(
                other.getLastUpdateDate() == null
                        ? null
                        : other.getLastUpdateDate().copy());
        this.setLastUpdatedBy(
                other.getLastUpdatedBy() == null
                        ? null
                        : other.getLastUpdatedBy().copy());
    }

    public UserFilter copy() {
        return new UserFilter(this);
    }
}
