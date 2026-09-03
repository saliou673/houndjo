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

    /**
     * The default organization currency code (ISO 4217), used when none is specified at registration.
     */
    public static final String DEFAULT_CURRENCY_CODE = "GNF";

    /**
     * The default organization timezone, used when none is specified at registration.
     */
    public static final String DEFAULT_TIMEZONE = "Africa/Conakry";
}
