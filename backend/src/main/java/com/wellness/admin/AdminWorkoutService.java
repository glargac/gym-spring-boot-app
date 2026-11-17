package com.wellness.admin;

import com.wellness.admin.AdminViews.WorkoutTemplateView;
import com.wellness.admin.WorkoutTemplateRequest.DayRequest;
import com.wellness.catalog.Goal;
import com.wellness.catalog.GoalRepository;
import com.wellness.catalog.TrainingLocation;
import com.wellness.catalog.TrainingLocationRepository;
import com.wellness.plan.RecommendationResponse.DayView;
import com.wellness.plan.RecommendationResponse.ExerciseView;
import com.wellness.web.InvalidFieldException;
import com.wellness.web.UnknownCodeException;
import com.wellness.workout.WorkoutDay;
import com.wellness.workout.WorkoutExercise;
import com.wellness.workout.WorkoutTemplate;
import com.wellness.workout.WorkoutTemplateRepository;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/** Only what is specific to workout templates: the generic CRUD flow lives in {@link AdminPlanService}. */
@Service
public class AdminWorkoutService extends AdminPlanService<WorkoutTemplate, WorkoutTemplateRequest, WorkoutTemplateView> {

    private final GoalRepository goals;
    private final TrainingLocationRepository locations;

    public AdminWorkoutService(WorkoutTemplateRepository templates, GoalRepository goals, TrainingLocationRepository locations) {
        super(templates, "Workout template");
        this.goals = goals;
        this.locations = locations;
    }

    /** Cross-field rule Bean Validation can't express on its own: the day list must match daysPerWeek and be numbered 1..n. */
    @Override
    protected void validate(WorkoutTemplateRequest r) {
        Set<Integer> expected = new TreeSet<>();
        for (int i = 1; i <= r.daysPerWeek(); i++) {
            expected.add(i);
        }
        Set<Integer> actual = new TreeSet<>(r.days().stream().map(DayRequest::dayIndex).toList());
        if (r.days().size() != r.daysPerWeek() || !actual.equals(expected)) {
            throw new InvalidFieldException("days", "must contain exactly " + r.daysPerWeek()
                    + " days, numbered 1.." + r.daysPerWeek() + " without repeats");
        }
    }

    @Override
    protected WorkoutTemplate newEntity(WorkoutTemplateRequest r) {
        var template = new WorkoutTemplate(goal(r), location(r), r.daysPerWeek(), r.name().trim());
        fillDays(template, r);
        return template;
    }

    @Override
    protected void replaceState(WorkoutTemplate template, WorkoutTemplateRequest r) {
        template.update(goal(r), location(r), r.daysPerWeek(), r.name().trim());
        template.clearDays(); // orphanRemoval deletes the old days and exercises
        fillDays(template, r);
    }

    @Override
    protected WorkoutTemplateView toView(WorkoutTemplate t) {
        List<DayView> days = t.getDays().stream()
                .map(d -> new DayView(d.getDayIndex(), d.getFocus(), d.getExercises().stream()
                        .map(e -> new ExerciseView(e.getName(), e.getSets(), e.getReps())).toList()))
                .toList();
        return new WorkoutTemplateView(t.getId(), t.getGoal().getCode(), t.getLocation().getCode(),
                t.getDaysPerWeek(), t.getName(), days);
    }

    private Goal goal(WorkoutTemplateRequest r) {
        return goals.findById(r.goalCode()).orElseThrow(() -> new UnknownCodeException("goalCode", r.goalCode()));
    }

    private TrainingLocation location(WorkoutTemplateRequest r) {
        return locations.findById(r.locationCode()).orElseThrow(() -> new UnknownCodeException("locationCode", r.locationCode()));
    }

    private static void fillDays(WorkoutTemplate template, WorkoutTemplateRequest r) {
        for (DayRequest d : r.days()) {
            var day = new WorkoutDay(template, d.dayIndex(), d.focus().trim());
            d.exercises().forEach(e -> day.addExercise(new WorkoutExercise(day, e.name().trim(), e.sets(), e.reps().trim())));
            template.addDay(day);
        }
    }
}
