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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Base class for the various attribute filters. It can be added to a criteria class as a member, to support the
 * following query parameters:
 * <pre>
 *      fieldName.equals='something'
 *      fieldName.notEquals='somethingElse'
 *      fieldName.specified=true
 *      fieldName.specified=false
 *      fieldName.in='something','other'
 *      fieldName.notIn='something','other'
 * </pre>
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Filter<FIELD_TYPE> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private FIELD_TYPE equals;
    private FIELD_TYPE notEquals;
    private Boolean specified;
    private List<FIELD_TYPE> in;
    private List<FIELD_TYPE> notIn;

    /**
     * <p>Constructor for Filter.</p>
     *
     * @param filter a {@link Filter} object.
     */
    public Filter(Filter<FIELD_TYPE> filter) {
        equals = filter.equals;
        notEquals = filter.notEquals;
        specified = filter.specified;
        in = filter.in == null ? null : new ArrayList<>(filter.in);
        notIn = filter.notIn == null ? null : new ArrayList<>(filter.notIn);
    }

    public static <T extends Filter<?>> T copy(T filter) {
        return filter == null ? null : (T) filter.copy();
    }

    /**
     * <p>copy.</p>
     *
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> copy() {
        return new Filter<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filter<?> filter = (Filter<?>) o;
        return (Objects.equals(equals, filter.equals)
                && Objects.equals(notEquals, filter.notEquals)
                && Objects.equals(specified, filter.specified)
                && Objects.equals(in, filter.in)
                && Objects.equals(notIn, filter.notIn));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(equals, notEquals, specified, in, notIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (getFilterName() + " ["
                + (getEquals() != null ? "equals=" + getEquals() + ", " : "")
                + (getNotEquals() != null ? "notEquals=" + getNotEquals() + ", " : "")
                + (getSpecified() != null ? "specified=" + getSpecified() + ", " : "")
                + (getIn() != null ? "in=" + getIn() + ", " : "")
                + (getNotIn() != null ? "notIn=" + getNotIn() : "")
                + "]");
    }

    /**
     * <p>getFilterName.</p>
     *
     * @return a {@link String} object.
     */
    protected String getFilterName() {
        return getClass().getSimpleName();
    }
}
