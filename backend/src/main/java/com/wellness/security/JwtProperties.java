package com.wellness.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed, immutable configuration bound from "app.jwt.*" (a record works as a @ConfigurationProperties class). */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration ttl) {

    public JwtProperties {
        if (ttl == null) {
            ttl = Duration.ofHours(1);
        }
    }
}
