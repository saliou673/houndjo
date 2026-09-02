package com.houndjo.domain.models;

import com.houndjo.domain.exceptions.RequiredFieldException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for common domain-level validation checks.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class DomainValidation {

    public static void checkRequiredField(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new RequiredFieldException(fieldName);
        }
    }
}
