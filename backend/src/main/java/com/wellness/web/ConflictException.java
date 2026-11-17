package com.wellness.web;

/** Valid request, but it clashes with the current state (e.g. deleting something that is still referenced). Reported as 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
