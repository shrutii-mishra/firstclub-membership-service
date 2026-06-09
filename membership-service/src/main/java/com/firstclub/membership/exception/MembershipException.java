package com.firstclub.membership.exception;

/**
 * Thrown when a business rule is violated.
 * E.g., trying to upgrade to the same tier, or subscribe when already active.
 */
public class MembershipException extends RuntimeException {
    public MembershipException(String message) {
        super(message);
    }
}
