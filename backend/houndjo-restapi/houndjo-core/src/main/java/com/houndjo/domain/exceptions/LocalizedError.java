package com.houndjo.domain.exceptions;

/**
 * Implemented by domain exceptions that carry a stable message {@code code} and the
 * {@code args} needed to interpolate it. Lets an inbound adapter (e.g. a REST
 * controller advice backed by a Spring {@code MessageSource}) resolve a localized,
 * user-facing message without the domain layer depending on any i18n framework.
 */
public interface LocalizedError {
    String getCode();

    Object[] getArgs();
}
