package com.wellness.user;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input DTO. There is deliberately no "role" field: clients can never choose their own role
 * (mass assignment / privilege escalation). The service always registers a USER.
 */
public record RegisterUserRequest(
        @NotBlank @Email @Size(max = 160) String email,
        // BCrypt only uses the first 72 bytes, so a longer password gives a false sense of security.
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 120) String fullName,
        @NotNull @Past LocalDate birthDate,
        @NotNull Sex sex,
        @Min(100) @Max(250) int heightCm,
        @NotNull @DecimalMin("30") @DecimalMax("300") BigDecimal weightKg,
        @NotNull ActivityLevel activityLevel) {

    /** A record's generated toString prints every component: mask the password so it never reaches the logs. */
    @Override
    public String toString() {
        return "RegisterUserRequest[email=" + email + ", fullName=" + fullName + ", password=***]";
    }
}
