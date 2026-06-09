package com.firstclub.membership.exception;

/**
 * Thrown when a requested resource (user, plan, tier, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
