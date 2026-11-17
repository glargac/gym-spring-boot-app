package com.wellness.web;

/** A request that parses fine but breaks a business rule on one field. Reported as 400 in the same shape as Bean Validation. */
public class InvalidFieldException extends RuntimeException {

    private final String field;

    public InvalidFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
