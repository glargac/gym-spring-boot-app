package com.wellness.plan;

import static com.wellness.support.Registrations.aRegistration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import com.wellness.user.PreferenceService;
import com.wellness.user.PreferencesRequest;
import com.wellness.user.UserService;
import com.wellness.web.NotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RecommendationServiceTest {

    @Autowired UserService users;
    @Autowired PreferenceService preferences;
    @Autowired RecommendationService recommendations;
    @Autowired JdbcTemplate jdbc; // runs on the same transaction/connection, so its deletes roll back with the test

    private Long userWith(String goal, String location, String diet, int days, String excluded) {
        return userWith(Sex.MALE, 178, "80", ActivityLevel.MODERATE, goal, location, diet, days, excluded);
    }

    private Long userWith(Sex sex, int heightCm, String weightKg, ActivityLevel level, String goal, String location,
                          String diet, int days, String excluded) {
        Long id = users.register(aRegistration().email("rec@example.com").sex(sex).heightCm(heightCm)
                .weightKg(weightKg).activityLevel(level).build()).getId();
        preferences.save(id, new PreferencesRequest(goal, location, diet, days, new BigDecimal("1.5"), excluded));
        return id;
    }

    private void deleteWorkouts(String goal, String location) {
        String templates = "select id from workout_template where goal_code = '" + goal + "'"
                + (location == null ? "" : " and location_code = '" + location + "'");
        jdbc.update("delete from workout_exercise where day_id in (select id from workout_day where template_id in (" + templates + "))");
        jdbc.update("delete from workout_day where template_id in (" + templates + ")");
        jdbc.update("delete from workout_template where id in (" + templates + ")");
    }

    private void deleteMealPlans(String goal) {
        jdbc.update("delete from meal where meal_plan_id in (select id from meal_plan where goal_code = '" + goal + "')");
        jdbc.update("delete from meal_plan where goal_code = '" + goal + "'");
    }

    @Test
    void exactMatchGivesTheGymPlanAndMealsScaledToTheTarget() {
        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "STANDARD", 3, null));

        assertThat(r.workout().name()).isEqualTo("Full body + cardio (3 days)");
        assertThat(r.workout().matchedBy()).startsWith("same goal and location");
        assertThat(r.workout().days()).hasSize(3);
        assertThat(r.workout().days().get(0).exercises()).extracting(e -> e.name()).startsWith("Squat");
        assertThat(r.mealPlan().name()).isEqualTo("Standard deficit");
        assertThat(r.targetKcal()).isEqualTo((int) Math.round(r.metrics().tdee() - 500));
        // the plan now adds up to the user's target (within rounding), instead of a fixed 1700 that contradicted it
        assertThat(r.mealPlan().totalKcal()).isBetween(r.targetKcal() - 3, r.targetKcal() + 3);
        assertThat(r.notes()).isEmpty();
    }

    @Test
    void everyGoalHasAWorkoutAndEveryGoalDietHasAMealPlan() {
        for (String goal : new String[] {"LOSE_WEIGHT", "GAIN_WEIGHT", "BUILD_MUSCLE", "BURN_FAT", "MAINTAIN"}) {
            for (String diet : new String[] {"STANDARD", "KETO", "VEGETARIAN", "VEGAN", "HIGH_PROTEIN", "MEDITERRANEAN"}) {
                for (String location : new String[] {"GYM", "OUTDOOR", "HOME"}) {
                    Long id = users.register(aRegistration().email(goal + diet + location + "@example.com").build()).getId();
                    preferences.save(id, new PreferencesRequest(goal, location, diet, 3, new BigDecimal("1"), null));
                    var r = recommendations.recommend(id);
                    assertThat(r.workout()).as("workout for %s/%s", goal, location).isNotNull();
                    assertThat(r.mealPlan()).as("meal plan for %s/%s", goal, diet).isNotNull();
                }
            }
        }
    }

    @Test
    void unavailableLocationFallsBackToAnotherLocationWithAnExplanation() {
        deleteWorkouts("LOSE_WEIGHT", "HOME");

        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "HOME", "STANDARD", 3, null));

        assertThat(r.workout()).isNotNull();
        assertThat(r.workout().matchedBy()).contains("different location");
    }

    @Test
    void prefersPlansThatDoNotExceedTheDaysTheUserOffered() {
        var more = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "STANDARD", 6, null));
        assertThat(more.workout().daysPerWeek()).isEqualTo(3);
    }

    @Test
    void dietIsAHardConstraintSoAVeganNeverGetsAnimalProducts() {
        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "VEGAN", 3, null));

        assertThat(r.mealPlan().name()).contains("Vegan");
        assertThat(r.mealPlan().meals()).extracting(m -> m.description().toLowerCase())
                .noneMatch(d -> d.matches(".*(chicken|fish|egg|cheese|yogurt|beef|salmon|tuna|turkey|butter).*"));
    }

    @Test
    void noMealPlanIsInventedWhenTheDietHasNoneForTheGoal() {
        deleteMealPlans("MAINTAIN");

        var r = recommendations.recommend(userWith("MAINTAIN", "GYM", "VEGAN", 3, null));

        assertThat(r.mealPlan()).isNull();
        assertThat(r.targetKcal()).isNull();
        assertThat(r.notes()).anyMatch(n -> n.contains("VEGAN"));
    }

    @Test
    void goalWithoutTemplatesReturnsNullsAndNotesInsteadOfFailing() {
        deleteWorkouts("BUILD_MUSCLE", null);
        deleteMealPlans("BUILD_MUSCLE");

        var r = recommendations.recommend(userWith("BUILD_MUSCLE", "GYM", "STANDARD", 4, null));

        assertThat(r.workout()).isNull();
        assertThat(r.mealPlan()).isNull();
        assertThat(r.notes()).hasSize(2);
        assertThat(r.metrics().bmi()).isPositive(); // health metrics are still useful
    }

    @Test
    void targetIsRaisedToTheSafeMinimumForASmallPerson() {
        var r = recommendations.recommend(
                userWith(Sex.FEMALE, 150, "45", ActivityLevel.SEDENTARY, "LOSE_WEIGHT", "HOME", "STANDARD", 3, null));

        assertThat(r.targetKcal()).isEqualTo(RecommendationService.MIN_DAILY_KCAL);
        assertThat(r.notes()).anyMatch(n -> n.contains("safe minimum"));
    }

    @Test
    void warnsAboutExcludedFoodsFoundInMeals() {
        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "STANDARD", 3, "Almonds, shrimp"));

        assertThat(r.notes()).hasSize(1).first().asString().contains("Greek yogurt and almonds");
    }

    @Test
    void needsPreferencesFirst() {
        Long id = users.register(aRegistration().email("nopref@example.com").build()).getId();

        assertThatThrownBy(() -> recommendations.recommend(id)).isInstanceOf(NotFoundException.class);
    }
}
