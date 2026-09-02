/*
 * Copyright 2016-2025 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.houndjo.domain.models.query.filter;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * Filter class for {@link Date} type attributes.
 *
 * @see DateFilter
 */
@NoArgsConstructor
public class DateFilter extends RangeFilter<Date> {

    @Serial
    private static final long serialVersionUID = 1L;

    public DateFilter(DateFilter filter) {
        super(filter);
    }

    @Override
    public DateFilter copy() {
        return new DateFilter(this);
    }

    @Override
    public DateFilter setEquals(Date equals) {
        super.setEquals(equals);
        return this;
    }

    @Override
    public DateFilter setNotEquals(Date equals) {
        super.setNotEquals(equals);
        return this;
    }

    @Override
    public DateFilter setIn(List<Date> in) {
        super.setIn(in);
        return this;
    }

    @Override
    public DateFilter setNotIn(List<Date> notIn) {
        super.setNotIn(notIn);
        return this;
    }

    @Override
    public DateFilter setGreaterThan(Date equals) {
        super.setGreaterThan(equals);
        return this;
    }

    @Override
    public DateFilter setLessThan(Date equals) {
        super.setLessThan(equals);
        return this;
    }

    @Override
    public DateFilter setGreaterThanOrEqual(Date equals) {
        super.setGreaterThanOrEqual(equals);
        return this;
    }

    @Override
    public DateFilter setLessThanOrEqual(Date equals) {
        super.setLessThanOrEqual(equals);
        return this;
    }
}
