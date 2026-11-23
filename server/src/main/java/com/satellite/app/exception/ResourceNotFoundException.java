package com.satellite.app.exception;

/**
 * Thrown when a constellation or satellite does not exist.
 * Extends {@link IllegalArgumentException} so existing callers that catch it keep working;
 * the REST layer maps it to 404 instead of 400.
 */
public class ResourceNotFoundException extends IllegalArgumentException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
