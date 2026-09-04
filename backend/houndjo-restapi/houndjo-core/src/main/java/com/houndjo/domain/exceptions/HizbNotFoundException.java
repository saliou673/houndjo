package com.houndjo.domain.exceptions;

/**
 * Thrown when no Quran verse can be found in the given hizb or quarter-hizb.
 */
public class HizbNotFoundException extends FunctionalException {
    public HizbNotFoundException(int hizb) {
        super("error.quran.hizb-not-found", "No data found for hizb " + hizb + ".", hizb);
    }
}
