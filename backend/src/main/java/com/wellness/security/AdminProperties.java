package com.wellness.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.admin")
public record AdminProperties(String initialPassword) {

    /** A record's generated toString would print the password. */
    @Override
    public String toString() {
        return "AdminProperties[initialPassword=***]";
    }
}
