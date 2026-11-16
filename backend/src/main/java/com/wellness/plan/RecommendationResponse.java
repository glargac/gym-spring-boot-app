package com.wellness.plan;

import com.wellness.health.HealthMetrics;
import java.util.List;

/** workout and mealPlan are null when nothing suitable exists; "notes" says why. */
public record RecommendationResponse(
        HealthMetrics metrics,
        Integer targetKcal,
        WorkoutView workout,
        MealPlanView mealPlan,
        List<String> notes) {

    public record WorkoutView(String name, String matchedBy, int daysPerWeek, List<DayView> days) {}

    public record DayView(int dayIndex, String focus, List<ExerciseView> exercises) {}

    public record ExerciseView(String name, int sets, String reps) {}

    public record MealPlanView(String name, int kcalAdjust, int totalKcal, List<MealView> meals) {}

    public record MealView(String type, String description, int kcal) {}
}
