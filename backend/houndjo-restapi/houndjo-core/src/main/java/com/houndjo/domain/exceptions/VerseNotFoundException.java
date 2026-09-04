package com.houndjo.domain.exceptions;

/**
 * Thrown when a Quran verse cannot be found by its surah and verse number.
 */
public class VerseNotFoundException extends FunctionalException {
    public VerseNotFoundException(int surahNumber, int verseNumber) {
        super(
                "error.quran.verse-not-found",
                "No verse found for surah " + surahNumber + ", verse " + verseNumber + ".",
                surahNumber,
                verseNumber);
    }
}
