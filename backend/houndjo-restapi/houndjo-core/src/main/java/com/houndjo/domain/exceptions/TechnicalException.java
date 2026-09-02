package com.houndjo.domain.exceptions;

import java.util.Arrays;

/**
 * Generic technical exception for unexpected errors or technical failures.
 * <p>
 * Use this for infrastructure/configuration issues or external dependency failures,
 * not for business rule violations.
 * <p>
 * Carries a stable {@code code} (a message key) plus the {@code args} needed to
 * interpolate it, so that the message can be resolved to any supported locale by
 * an inbound adapter (e.g. a Spring {@code MessageSource}) without the domain
 * layer depending on any i18n framework. {@link #getMessage()} keeps returning an
 * English fallback, used for logs and as a safety net if a translation is missing.
 */
public class TechnicalException extends RuntimeException implements LocalizedError {
    private final String code;
    private final transient Object[] args;

    public TechnicalException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public TechnicalException(String code, String message, Throwable cause, Object... args) {
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
