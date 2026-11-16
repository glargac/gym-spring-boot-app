package com.wellness.user;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Catalog values are sent as codes (e.g. "LOSE_WEIGHT"). The service checks they exist, because the DB has no way to say "unknown code" nicely. */
public record PreferencesRequest(
        @NotBlank String goalCode,
        @NotBlank String locationCode,
        @NotBlank String dietTypeCode,
        @Min(1) @Max(7) int daysPerWeek,
        @NotNull @DecimalMin("0.5") @DecimalMax("8.0") @Digits(integer = 1, fraction = 1) BigDecimal hoursPerDay,
        @Size(max = 255) String excludedFoods) {
}
