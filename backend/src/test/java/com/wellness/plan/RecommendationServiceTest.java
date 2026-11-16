package com.wellness.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import com.wellness.user.PreferenceService;
import com.wellness.user.PreferencesRequest;
import com.wellness.user.RegisterUserRequest;
import com.wellness.user.UserService;
import com.wellness.web.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RecommendationServiceTest {

    @Autowired UserService users;
    @Autowired PreferenceService preferences;
    @Autowired RecommendationService recommendations;

    private Long userWith(String goal, String location, String diet, int days, String excluded) {
        Long id = users.register(new RegisterUserRequest("rec@example.com", "s3cret-pass", "Rec", LocalDate.of(1990, 1, 1),
                Sex.MALE, 178, new BigDecimal("80"), ActivityLevel.MODERATE)).getId();
        preferences.save(id, new PreferencesRequest(goal, location, diet, days, new BigDecimal("1.5"), excluded));
        return id;
    }

    @Test
    void exactMatchGivesTheGymPlanAndAMealPlanWithTargetKcal() {
        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "STANDARD", 3, null));

        assertThat(r.workout().name()).isEqualTo("Full body + cardio (3 days)");
        assertThat(r.workout().matchedBy()).startsWith("same goal and location");
        assertThat(r.workout().days()).hasSize(3);
        assertThat(r.workout().days().get(0).exercises()).extracting(e -> e.name()).startsWith("Squat");
        assertThat(r.mealPlan().name()).isEqualTo("Standard deficit");
        assertThat(r.mealPlan().totalKcal()).isEqualTo(1700);
        assertThat(r.targetKcal()).isEqualTo((int) Math.round(r.metrics().tdee() - 500));
        assertThat(r.notes()).isEmpty();
    }

    @Test
    void unavailableLocationFallsBackToAnotherLocationWithAnExplanation() {
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
    void dietIsAHardConstraintSoNoMealPlanIsInventedForVegans() {
        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "VEGAN", 3, null));

        assertThat(r.mealPlan()).isNull();
        assertThat(r.targetKcal()).isNull();
        assertThat(r.notes()).anyMatch(n -> n.contains("VEGAN"));
    }

    @Test
    void goalWithoutTemplatesReturnsNullsAndNotesInsteadOfFailing() {
        var r = recommendations.recommend(userWith("BUILD_MUSCLE", "GYM", "STANDARD", 4, null));

        assertThat(r.workout()).isNull();
        assertThat(r.mealPlan()).isNull();
        assertThat(r.notes()).hasSize(2);
        assertThat(r.metrics().bmi()).isPositive(); // health metrics are still useful
    }

    @Test
    void warnsAboutExcludedFoodsFoundInMeals() {
        var r = recommendations.recommend(userWith("LOSE_WEIGHT", "GYM", "STANDARD", 3, "Almonds, shrimp"));

        assertThat(r.notes()).hasSize(1).first().asString().contains("Greek yogurt and almonds");
    }

    @Test
    void needsPreferencesFirst() {
        Long id = users.register(new RegisterUserRequest("nopref@example.com", "s3cret-pass", "NoPref", LocalDate.of(1990, 1, 1),
                Sex.MALE, 178, new BigDecimal("80"), ActivityLevel.MODERATE)).getId();

        assertThatThrownBy(() -> recommendations.recommend(id)).isInstanceOf(NotFoundException.class);
    }
}
