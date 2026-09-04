package com.houndjo.domain.exceptions;

/**
 * Thrown when a Quran surah cannot be found by its number.
 */
public class SurahNotFoundException extends FunctionalException {
    public SurahNotFoundException(int number) {
        super("error.quran.surah-not-found", "No surah found with number " + number + ".", number);
    }
}
