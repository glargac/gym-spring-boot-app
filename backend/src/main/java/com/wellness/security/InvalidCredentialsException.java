package com.wellness.security;

/** One message for "no such user" and "wrong password": telling them apart would let attackers list registered emails. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
