package com.houndjo.domain.exceptions;

import java.util.Arrays;

/**
 * Base exception for functional (business) errors.
 * <p>
 * Use this type for domain or application errors that are not technical failures
 * (e.g., invalid user input, business rule violation).
 * <p>
 * Carries a stable {@code code} (a message key) plus the {@code args} needed to
 * interpolate it, so that the message can be resolved to any supported locale by
 * an inbound adapter (e.g. a Spring {@code MessageSource}) without the domain
 * layer depending on any i18n framework. {@link #getMessage()} keeps returning an
 * English fallback, used for logs and as a safety net if a translation is missing.
 */
public class FunctionalException extends RuntimeException implements LocalizedError {
    private final String code;
    private final transient Object[] args;

    public FunctionalException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public FunctionalException(String code, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Object[] getArgs() {
        return args == null ? new Object[0] : Arrays.copyOf(args, args.length);
    }
}
