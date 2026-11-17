package com.wellness.admin;

import com.wellness.meal.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MealPlanRequest(
        @NotBlank String goalCode,
        @NotBlank String dietTypeCode,
        @Min(-2000) @Max(2000) int kcalAdjust,
        @NotBlank @Size(max = 120) String name,
        @NotEmpty @Valid List<MealRequest> meals) {

    /** kcal is the BASE weight of the meal in a ~2000 kcal day; the recommendation scales it to each user. */
    public record MealRequest(
            @NotNull MealType mealType,
            @NotBlank @Size(max = 255) String description,
            @Min(1) @Max(3000) int kcal) {}
}
