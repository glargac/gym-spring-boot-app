package com.wellness.web;

/** A syntactically valid request that points to a catalog row that does not exist. Reported as 400 on that field. */
public class UnknownCodeException extends InvalidFieldException {

    public UnknownCodeException(String field, String code) {
        super(field, "Unknown value '" + code + "'");
    }
}
