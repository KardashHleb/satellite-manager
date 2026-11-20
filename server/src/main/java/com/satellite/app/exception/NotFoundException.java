package com.satellite.app.exception;

/**
 * Thrown when a constellation or a satellite does not exist.
 * Extends IllegalArgumentException so existing callers keep working; the REST controller maps it to 404.
 */
public class NotFoundException extends IllegalArgumentException {
    public NotFoundException(String message) {
        super(message);
    }
}
