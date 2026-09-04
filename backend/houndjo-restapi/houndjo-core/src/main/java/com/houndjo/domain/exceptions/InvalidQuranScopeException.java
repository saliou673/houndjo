package com.houndjo.domain.exceptions;

/**
 * Thrown when a QURAN course's juz scope is inconsistent ({@code fromJuz} greater than {@code toJuz}).
 */
public class InvalidQuranScopeException extends FunctionalException {
    public InvalidQuranScopeException(int fromJuz, int toJuz) {
        super(
                "error.course.invalid-quran-scope",
                "Invalid Quran scope: fromJuz " + fromJuz + " must be <= toJuz " + toJuz + ".",
                fromJuz,
                toJuz);
    }
}
