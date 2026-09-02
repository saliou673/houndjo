package com.houndjo.domain.models.appconfiguration;

import com.houndjo.domain.models.query.AuditableFilter;
import com.houndjo.domain.models.query.filter.AppConfigurationCategoryFilter;
import com.houndjo.domain.models.query.filter.BooleanFilter;
import com.houndjo.domain.models.query.filter.LongFilter;
import com.houndjo.domain.models.query.filter.StringFilter;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;

/**
 * Filter criteria for querying {@link com.houndjo.domain.models.appconfiguration.AppConfiguration} entries.
 * Null fields mean no constraint on that attribute.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public final class AppConfigurationFilter extends AuditableFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Filter on the surrogate identifier.
     */
    private LongFilter id;
    /**
     * Filter on the configuration category.
     */
    private AppConfigurationCategoryFilter category;
    /**
     * Filter on the short code.
     */
    private StringFilter code;
    /**
     * Filter on the display label.
     */
    private StringFilter label;
    /**
     * Filter on the active flag.
     */
    private BooleanFilter active;

    public AppConfigurationFilter(AppConfigurationFilter other) {
        this.id = other.id == null ? null : other.id.copy();
        this.category = other.category == null ? null : other.category.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.label = other.label == null ? null : other.label.copy();
        this.active = other.active == null ? null : other.active.copy();
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

    public AppConfigurationFilter copy() {
        return new AppConfigurationFilter(this);
    }
}
