package com.wellness.web;

import com.wellness.security.InvalidCredentialsException;
import com.wellness.user.EmailAlreadyUsedException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** One place that turns exceptions into HTTP responses (cross-cutting concern, keeps controllers clean). */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Bean Validation failed: report every failing field, not just the first. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail onNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Same shape as a Bean Validation failure, so clients handle one error format. */
    @ExceptionHandler(UnknownCodeException.class)
    public ProblemDetail onUnknownCode(UnknownCodeException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", Map.of(ex.getField(), ex.getMessage()));
        return pd;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail onBadCredentials(InvalidCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 409 Conflict: the request is valid but clashes with existing state. */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail onEmailTaken(EmailAlreadyUsedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Malformed JSON, wrong types or an unknown enum value. The message is generic on purpose: the raw one leaks internals. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onUnreadable(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Malformed request body or invalid value (check enum values and date format yyyy-MM-dd)");
    }
}
