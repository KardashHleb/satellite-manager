package com.satellite.app.exception;

/**
 * A constellation or a satellite that the request refers to does not exist (mapped to HTTP 404).
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
