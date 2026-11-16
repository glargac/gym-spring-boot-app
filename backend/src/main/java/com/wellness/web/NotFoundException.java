package com.wellness.web;

/** Shared by every feature, so it lives in the web package rather than in one domain package. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
