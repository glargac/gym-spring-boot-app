package com.wellness.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Composed annotation: the rule lives in ONE place instead of a SpEL string copied onto every method.
 * Convention: the method must have a parameter named "userId" (needs the -parameters compiler flag, which Boot's parent enables).
 * authentication.name is the JWT "sub", which is the user's id.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
public @interface OwnerOrAdmin {
}
