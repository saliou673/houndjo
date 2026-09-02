package com.houndjo.domain.exceptions;

/**
 * Thrown when a mandatory field is missing or blank.
 */
public class RequiredFieldException extends FunctionalException {
    public RequiredFieldException(String fieldName) {
        super("error.validation.required-field", fieldName + " must not be empty.", fieldName);
    }
}
