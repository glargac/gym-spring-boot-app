package com.wellness.admin;

import com.wellness.plan.RecommendationResponse.DayView;
import com.wellness.plan.RecommendationResponse.MealView;
import java.util.List;

/** Read models for the admin screens. Plans here show the STORED values (meal kcal are unscaled base weights). */
public final class AdminViews {

    private AdminViews() {}

    public record WorkoutTemplateView(Long id, String goalCode, String locationCode, int daysPerWeek, String name,
                                      List<DayView> days) {}

    public record MealPlanView(Long id, String goalCode, String dietTypeCode, int kcalAdjust, String name,
                               List<MealView> meals) {}
}
