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

import java.time.ZonedDateTime;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * Filter class for {@link ZonedDateTime} type attributes.
 *
 * @see com.houndjo.domain.models.query.filter.RangeFilter
 */
@NoArgsConstructor
public class ZonedDateTimeFilter extends RangeFilter<ZonedDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for ZonedDateTimeFilter.</p>
     *
     * @param filter a {@link ZonedDateTimeFilter} object.
     */
    public ZonedDateTimeFilter(ZonedDateTimeFilter filter) {
        super(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter copy() {
        return new ZonedDateTimeFilter(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setEquals(ZonedDateTime equals) {
        super.setEquals(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setNotEquals(ZonedDateTime equals) {
        super.setNotEquals(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setIn(List<ZonedDateTime> in) {
        super.setIn(in);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setNotIn(List<ZonedDateTime> notIn) {
        super.setNotIn(notIn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setGreaterThan(ZonedDateTime equals) {
        super.setGreaterThan(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setLessThan(ZonedDateTime equals) {
        super.setLessThan(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setGreaterThanOrEqual(ZonedDateTime equals) {
        super.setGreaterThanOrEqual(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTimeFilter setLessThanOrEqual(ZonedDateTime equals) {
        super.setLessThanOrEqual(equals);
        return this;
    }
}
