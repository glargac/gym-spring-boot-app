package com.wellness.web;

/** A syntactically valid request that points to a catalog row that does not exist. Reported as 400 on that field. */
public class UnknownCodeException extends RuntimeException {

    private final String field;

    public UnknownCodeException(String field, String code) {
        super("Unknown value '" + code + "'");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
