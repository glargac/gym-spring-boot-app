package com.wellness.plan;

import com.wellness.user.UserPreference;
import com.wellness.workout.WorkoutTemplate;
import com.wellness.workout.WorkoutTemplateRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** First choice: same goal and same place, with the closest number of days. */
@Component
@Order(1) // lower number = tried first when Spring injects List<WorkoutMatchStrategy>
public class SameLocationWorkoutMatch implements WorkoutMatchStrategy {

    private final WorkoutTemplateRepository templates;

    public SameLocationWorkoutMatch(WorkoutTemplateRepository templates) {
        this.templates = templates;
    }

    @Override
    public Optional<WorkoutTemplate> find(UserPreference p) {
        return WorkoutMatchStrategy.bestFit(
                templates.findByGoalCodeAndLocationCodeOrderById(p.getGoal().getCode(), p.getLocation().getCode()),
                p.getDaysPerWeek());
    }

    @Override
    public String describe() {
        return "same goal and location, closest days per week";
    }
}
