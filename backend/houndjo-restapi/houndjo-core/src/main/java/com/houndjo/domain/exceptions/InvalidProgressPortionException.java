package com.houndjo.domain.exceptions;

import com.houndjo.domain.enumerations.ProgressFlow;

/**
 * Thrown when a progress record's portion type does not match its {@link ProgressFlow}
 * (e.g. a lesson portion given for a {@code SABAK} record).
 */
public class InvalidProgressPortionException extends FunctionalException {
    public InvalidProgressPortionException(ProgressFlow flow) {
        super("error.progress.invalid-portion", "Portion type does not match flow " + flow + ".", flow);
    }
}
