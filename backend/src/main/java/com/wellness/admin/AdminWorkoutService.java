package com.wellness.admin;

import com.wellness.admin.AdminViews.WorkoutTemplateView;
import com.wellness.admin.WorkoutTemplateRequest.DayRequest;
import com.wellness.catalog.GoalRepository;
import com.wellness.catalog.TrainingLocationRepository;
import com.wellness.plan.RecommendationResponse.DayView;
import com.wellness.plan.RecommendationResponse.ExerciseView;
import com.wellness.web.InvalidFieldException;
import com.wellness.web.NotFoundException;
import com.wellness.web.UnknownCodeException;
import com.wellness.workout.WorkoutDay;
import com.wellness.workout.WorkoutExercise;
import com.wellness.workout.WorkoutTemplate;
import com.wellness.workout.WorkoutTemplateRepository;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminWorkoutService {

    private final WorkoutTemplateRepository templates;
    private final GoalRepository goals;
    private final TrainingLocationRepository locations;

    public AdminWorkoutService(WorkoutTemplateRepository templates, GoalRepository goals, TrainingLocationRepository locations) {
        this.templates = templates;
        this.goals = goals;
        this.locations = locations;
    }

    @Transactional(readOnly = true)
    public List<WorkoutTemplateView> list() {
        return templates.findAll(Sort.by("id")).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public WorkoutTemplateView get(Long id) {
        return toView(find(id));
    }

    @Transactional
    public WorkoutTemplateView create(WorkoutTemplateRequest r) {
        validateDays(r);
        var template = new WorkoutTemplate(goal(r), location(r), r.daysPerWeek(), r.name().trim());
        fillDays(template, r);
        return toView(templates.saveAndFlush(template)); // flush so generated ids exist for the response
    }

    /** Replace semantics (PUT): the old days/exercises are removed and the new ones inserted, in one transaction. */
    @Transactional
    public WorkoutTemplateView update(Long id, WorkoutTemplateRequest r) {
        validateDays(r);
        WorkoutTemplate template = find(id);
        template.update(goal(r), location(r), r.daysPerWeek(), r.name().trim());
        template.clearDays();
        fillDays(template, r);
        return toView(templates.saveAndFlush(template));
    }

    @Transactional
    public void delete(Long id) {
        templates.delete(find(id)); // cascade REMOVE deletes the days and their exercises
    }

    private WorkoutTemplate find(Long id) {
        return templates.findById(id).orElseThrow(() -> new NotFoundException("Workout template " + id + " not found"));
    }

    private com.wellness.catalog.Goal goal(WorkoutTemplateRequest r) {
        return goals.findById(r.goalCode()).orElseThrow(() -> new UnknownCodeException("goalCode", r.goalCode()));
    }

    private com.wellness.catalog.TrainingLocation location(WorkoutTemplateRequest r) {
        return locations.findById(r.locationCode()).orElseThrow(() -> new UnknownCodeException("locationCode", r.locationCode()));
    }

    /** Cross-field rule Bean Validation can't express on its own: the day list must match daysPerWeek and be numbered 1..n. */
    private static void validateDays(WorkoutTemplateRequest r) {
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

    private static void fillDays(WorkoutTemplate template, WorkoutTemplateRequest r) {
        for (DayRequest d : r.days()) {
            var day = new WorkoutDay(template, d.dayIndex(), d.focus().trim());
            d.exercises().forEach(e -> day.addExercise(new WorkoutExercise(day, e.name().trim(), e.sets(), e.reps().trim())));
            template.addDay(day);
        }
    }

    private WorkoutTemplateView toView(WorkoutTemplate t) {
        List<DayView> days = t.getDays().stream()
                .map(d -> new DayView(d.getDayIndex(), d.getFocus(), d.getExercises().stream()
                        .map(e -> new ExerciseView(e.getName(), e.getSets(), e.getReps())).toList()))
                .toList();
        return new WorkoutTemplateView(t.getId(), t.getGoal().getCode(), t.getLocation().getCode(),
                t.getDaysPerWeek(), t.getName(), days);
    }
}
