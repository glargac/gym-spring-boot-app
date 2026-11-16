package com.wellness.user;

/** Business-rule violation. Unchecked so it propagates to the exception handler without throws clauses. */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email already registered: " + email);
    }
}
