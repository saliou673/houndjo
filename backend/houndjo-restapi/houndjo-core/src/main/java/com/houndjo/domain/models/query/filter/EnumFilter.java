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

import lombok.NoArgsConstructor;

/**
 * Filter class for {@link Enum} type attributes.
 *
 * @param <E> the enum type
 * @see Filter
 */
@NoArgsConstructor
public class EnumFilter<E extends Enum<E>> extends Filter<E> {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for EnumFilter.</p>
     *
     * @param filter a {@link EnumFilter} object.
     */
    public EnumFilter(EnumFilter<E> filter) {
        super(filter);
    }

    /**
     * <p>copy.</p>
     *
     * @return a {@link EnumFilter} object.
     */
    @Override
    public EnumFilter<E> copy() {
        return new EnumFilter<>(this);
    }
}
