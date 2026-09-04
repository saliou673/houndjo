package com.houndjo.domain.exceptions;

/**
 * Thrown when no Quran verse can be found on the given Mushaf page.
 */
public class PageNotFoundException extends FunctionalException {
    public PageNotFoundException(int page) {
        super("error.quran.page-not-found", "No data found for page " + page + ".", page);
    }
}
