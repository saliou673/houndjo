package com.houndjo.domain.exceptions;

/**
 * Thrown when a database operation fails at the infrastructure level.
 */
public class DataBaseException extends TechnicalException {
    public DataBaseException(String detail) {
        super("error.technical.database", "A database error occurred: " + detail, detail);
    }

    public DataBaseException(String detail, Throwable cause) {
        super("error.technical.database", "A database error occurred: " + detail, cause, detail);
    }
}
