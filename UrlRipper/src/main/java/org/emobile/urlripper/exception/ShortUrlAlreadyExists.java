package org.emobile.urlripper.exception;

public class ShortUrlAlreadyExists extends RuntimeException {
    public ShortUrlAlreadyExists(String message) {
        super(message);
    }
}
