package com.wellness.plan;

import com.wellness.user.UserPreference;
import com.wellness.workout.WorkoutTemplate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Strategy pattern: one way of finding a workout for a user's preferences.
 * RecommendationService receives ALL implementations and tries them in order, so there is no if/else chain to edit:
 * a new rule is a new class (Open/Closed Principle).
 */
public interface WorkoutMatchStrategy {

    Optional<WorkoutTemplate> find(UserPreference preference);

    /** Shown to the client so a recommendation is explainable ("why this plan?"). */
    String describe();

    /**
     * Best fit on days per week: never ask the user for more days than they offered if avoidable
     * (plans with days <= requested come first), then the closest number, then the lowest id for a stable result.
     */
    static Optional<WorkoutTemplate> bestFit(List<WorkoutTemplate> candidates, int requestedDays) {
        return candidates.stream().min(
                Comparator.comparingInt((WorkoutTemplate t) -> t.getDaysPerWeek() > requestedDays ? 1 : 0)
                        .thenComparingInt(t -> Math.abs(t.getDaysPerWeek() - requestedDays))
                        .thenComparing(WorkoutTemplate::getId));
    }
}
