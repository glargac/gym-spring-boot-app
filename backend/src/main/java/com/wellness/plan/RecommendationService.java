package com.wellness.plan;

import com.wellness.health.HealthMetrics;
import com.wellness.health.HealthMetricsService;
import com.wellness.meal.MealPlan;
import com.wellness.meal.MealPlanRepository;
import com.wellness.plan.RecommendationResponse.DayView;
import com.wellness.plan.RecommendationResponse.ExerciseView;
import com.wellness.plan.RecommendationResponse.MealPlanView;
import com.wellness.plan.RecommendationResponse.MealView;
import com.wellness.plan.RecommendationResponse.WorkoutView;
import com.wellness.user.AppUser;
import com.wellness.user.UserPreference;
import com.wellness.user.UserPreferenceRepository;
import com.wellness.user.UserRepository;
import com.wellness.web.NotFoundException;
import com.wellness.workout.WorkoutTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    /** Common safety floor for an unsupervised plan; a large deficit on a small person would otherwise go below it. */
    static final int MIN_DAILY_KCAL = 1200;

    private final UserRepository users;
    private final UserPreferenceRepository preferences;
    private final HealthMetricsService health;
    private final List<WorkoutMatchStrategy> workoutStrategies; // injected already sorted by @Order
    private final MealPlanRepository mealPlans;

    public RecommendationService(UserRepository users, UserPreferenceRepository preferences,
                                 HealthMetricsService health, List<WorkoutMatchStrategy> workoutStrategies,
                                 MealPlanRepository mealPlans) {
        this.users = users;
        this.preferences = preferences;
        this.health = health;
        this.workoutStrategies = workoutStrategies;
        this.mealPlans = mealPlans;
    }

    /** One transaction: the lazy days/exercises/meals are read while mapping to views (open-in-view is off). */
    @Transactional(readOnly = true)
    public RecommendationResponse recommend(Long userId) {
        AppUser user = users.findById(userId).orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        UserPreference pref = preferences.findById(userId).orElseThrow(() ->
                new NotFoundException("User " + userId + " has no preferences yet: PUT /api/users/" + userId + "/preferences first"));

        HealthMetrics metrics = health.calculate(user.getSex(), user.getBirthDate(), user.getHeightCm(),
                user.getWeightKg().doubleValue(), user.getActivityLevel());

        List<String> notes = new ArrayList<>();
        WorkoutView workout = pickWorkout(pref, notes);
        MealPlan mealPlan = pickMealPlan(pref, notes);

        Integer targetKcal = null;
        MealPlanView mealView = null;
        if (mealPlan != null) {
            long raw = Math.round(metrics.tdee() + mealPlan.getKcalAdjust());
            targetKcal = (int) Math.max(raw, MIN_DAILY_KCAL);
            if (raw < MIN_DAILY_KCAL) {
                notes.add("Your calculated target (" + raw + " kcal) is below the safe minimum, so it was raised to "
                        + MIN_DAILY_KCAL + " kcal. Talk to a professional before eating less than that.");
            }
            mealView = toView(mealPlan, targetKcal);
        }
        if (mealView != null) {
            notes.addAll(excludedFoodWarnings(pref.getExcludedFoods(), mealView));
        }
        return new RecommendationResponse(metrics, targetKcal, workout, mealView, notes);
    }

    private WorkoutView pickWorkout(UserPreference pref, List<String> notes) {
        for (WorkoutMatchStrategy strategy : workoutStrategies) {
            var found = strategy.find(pref);
            if (found.isPresent()) {
                return toView(found.get(), strategy.describe());
            }
        }
        notes.add("No workout plan exists yet for goal " + pref.getGoal().getCode());
        return null;
    }

    /**
     * Diet is a HARD constraint (ethics, allergies, health): unlike location, we never fall back to another diet,
     * because handing a vegan a chicken plan is worse than handing them nothing.
     */
    private MealPlan pickMealPlan(UserPreference pref, List<String> notes) {
        var found = mealPlans.findByGoalCodeAndDietTypeCodeOrderById(pref.getGoal().getCode(), pref.getDietType().getCode());
        if (found.isEmpty()) {
            notes.add("No meal plan exists yet for goal " + pref.getGoal().getCode()
                    + " with diet " + pref.getDietType().getCode());
            return null;
        }
        return found.get(0);
    }

    /** Naive substring match on the meal description: it can miss synonyms and plurals, so it is a warning, not a guarantee. */
    private List<String> excludedFoodWarnings(String excludedFoods, MealPlanView plan) {
        if (excludedFoods == null || excludedFoods.isBlank()) {
            return List.of();
        }
        List<String> warnings = new ArrayList<>();
        Arrays.stream(excludedFoods.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .forEach(food -> plan.meals().stream()
                        .filter(m -> m.description().toLowerCase(Locale.ROOT).contains(food))
                        .forEach(m -> warnings.add("Meal '" + m.description() + "' may contain '" + food + "', which you excluded")));
        return warnings;
    }

    private WorkoutView toView(WorkoutTemplate t, String matchedBy) {
        List<DayView> days = t.getDays().stream()
                .map(d -> new DayView(d.getDayIndex(), d.getFocus(), d.getExercises().stream()
                        .map(e -> new ExerciseView(e.getName(), e.getSets(), e.getReps())).toList()))
                .toList();
        return new WorkoutView(t.getName(), matchedBy, t.getDaysPerWeek(), days);
    }

    /**
     * The stored kcal are proportions of a base day. Scaling every meal by target / baseTotal makes the plan add up
     * to the user's own target instead of a fixed number that contradicts it.
     */
    private MealPlanView toView(MealPlan p, int targetKcal) {
        int baseTotal = p.getMeals().stream().mapToInt(m -> m.getKcal()).sum();
        double factor = baseTotal == 0 ? 1.0 : (double) targetKcal / baseTotal;
        List<MealView> meals = p.getMeals().stream()
                .map(m -> new MealView(m.getMealType().name(), m.getDescription(), (int) Math.round(m.getKcal() * factor)))
                .toList();
        int total = meals.stream().mapToInt(MealView::kcal).sum(); // may differ from the target by a few kcal of rounding
        return new MealPlanView(p.getName(), p.getKcalAdjust(), total, Math.round(factor * 100) / 100.0, meals);
    }
}
