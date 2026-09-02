package com.houndjo.domain.models.query.filter;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import java.io.Serial;
import lombok.NoArgsConstructor;

/**
 * Filter class for {@link AppConfigurationCategory} enum attributes.
 */
@NoArgsConstructor
public class AppConfigurationCategoryFilter extends EnumFilter<AppConfigurationCategory> {

    @Serial
    private static final long serialVersionUID = 1L;

    public AppConfigurationCategoryFilter(AppConfigurationCategoryFilter filter) {
        super(filter);
    }

    @Override
    public AppConfigurationCategoryFilter copy() {
        return new AppConfigurationCategoryFilter(this);
    }
}
