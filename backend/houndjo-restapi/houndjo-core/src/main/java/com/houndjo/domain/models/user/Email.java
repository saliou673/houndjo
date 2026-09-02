package com.houndjo.domain.models.user;

import static com.houndjo.domain.constants.DomainConstants.EMAIL_PATTERN;

import com.houndjo.domain.exceptions.InvalidUserNameException;

/**
 * Value object representing a validated, normalized (lowercase) email address.
 *
 * @param value the raw email string; validated and lowercased on construction
 */
public record Email(String value) {

    public Email {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidUserNameException(value);
        }

        value = value.toLowerCase();
    }
}
