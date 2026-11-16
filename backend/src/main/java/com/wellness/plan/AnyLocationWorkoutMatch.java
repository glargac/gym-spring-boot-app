package com.wellness.plan;

import com.wellness.user.UserPreference;
import com.wellness.workout.WorkoutTemplate;
import com.wellness.workout.WorkoutTemplateRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Fallback: location is a soft preference, so a plan for the right goal elsewhere beats no plan. */
@Component
@Order(2)
public class AnyLocationWorkoutMatch implements WorkoutMatchStrategy {

    private final WorkoutTemplateRepository templates;

    public AnyLocationWorkoutMatch(WorkoutTemplateRepository templates) {
        this.templates = templates;
    }

    @Override
    public Optional<WorkoutTemplate> find(UserPreference p) {
        return WorkoutMatchStrategy.bestFit(templates.findByGoalCodeOrderById(p.getGoal().getCode()), p.getDaysPerWeek());
    }

    @Override
    public String describe() {
        return "same goal, different location (no plan for your location yet)";
    }
}
