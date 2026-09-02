package com.houndjo.domain.exceptions;

/**
 * Thrown when attempting to create or rename a role group to a name that is already taken.
 */
public class RoleGroupNameAlreadyExistsException extends FunctionalException {
    public RoleGroupNameAlreadyExistsException(String name) {
        super("error.role-group.name-already-exists", "A role group named '" + name + "' already exists.", name);
    }
}
