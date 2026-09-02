package com.houndjo.domain.exceptions;

/**
 * Thrown when a role group cannot be found by the given identifier or name.
 */
public class RoleGroupNotFoundException extends FunctionalException {
    public RoleGroupNotFoundException(Long id) {
        super("error.role-group.not-found", "No role group found with id " + id + ".", id);
    }
}
