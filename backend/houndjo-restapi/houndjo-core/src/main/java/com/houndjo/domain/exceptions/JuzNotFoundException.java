package com.houndjo.domain.exceptions;

/**
 * Thrown when no Quran verse can be found in the given juz.
 */
public class JuzNotFoundException extends FunctionalException {
    public JuzNotFoundException(int juz) {
        super("error.quran.juz-not-found", "No data found for juz " + juz + ".", juz);
    }
}
