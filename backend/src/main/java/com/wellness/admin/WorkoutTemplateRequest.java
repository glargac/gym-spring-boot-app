package com.wellness.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** A whole workout template in one payload (an aggregate): the admin sends the full state and we replace it. */
public record WorkoutTemplateRequest(
        @NotBlank String goalCode,
        @NotBlank String locationCode,
        @Min(1) @Max(7) int daysPerWeek,
        @NotBlank @Size(max = 120) String name,
        @NotEmpty @Valid List<DayRequest> days) {

    public record DayRequest(
            @Min(1) @Max(7) int dayIndex,
            @NotBlank @Size(max = 120) String focus,
            @NotEmpty @Valid List<ExerciseRequest> exercises) {}

    public record ExerciseRequest(
            @NotBlank @Size(max = 120) String name,
            @Min(1) @Max(20) int sets,
            @NotBlank @Size(max = 20) String reps) {}
}
