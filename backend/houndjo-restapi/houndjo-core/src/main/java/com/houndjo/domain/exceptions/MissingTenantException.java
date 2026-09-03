package com.houndjo.domain.exceptions;

/**
 * Thrown when a business route requires an active organization but none could be resolved
 * from the current security context.
 */
public class MissingTenantException extends FunctionalException {
    public MissingTenantException() {
        super("error.tenant.missing", "No active organization found in the current security context.");
    }
}
