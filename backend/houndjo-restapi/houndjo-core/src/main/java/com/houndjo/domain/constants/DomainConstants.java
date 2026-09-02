package com.houndjo.domain.constants;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Domain constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DomainConstants {

    /**
     * Regex to validate accepted email.
     */
    public static final String EMAIL_REGEX_PATTERN = "^[\\w!#$%&'*+/=?`{|}~^.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    /**
     * Pattern to validate accepted email.
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX_PATTERN, Pattern.CASE_INSENSITIVE);

    /**
     * The default user language.
     */
    public static final String DEFAULT_LANGUAGE = "fr";
}
