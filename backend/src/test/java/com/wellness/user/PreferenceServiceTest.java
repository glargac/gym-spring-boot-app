package com.wellness.user;

import static com.wellness.support.Registrations.aRegistration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wellness.health.HealthMetricsService.Sex;
import com.wellness.web.NotFoundException;
import com.wellness.web.UnknownCodeException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PreferenceServiceTest {

    @Autowired UserService users;
    @Autowired PreferenceService preferences;

    private Long newUserId() {
        return users.register(aRegistration().email("pref@example.com").sex(Sex.OTHER).heightCm(170).weightKg("70").build()).getId();
    }

    private PreferencesRequest request(String goal, int days) {
        return new PreferencesRequest(goal, "GYM", "STANDARD", days, new BigDecimal("1.5"), "peanuts");
    }

    @Test
    void createsThenReplacesPreferences() {
        Long id = newUserId();

        PreferencesResponse created = preferences.save(id, request("LOSE_WEIGHT", 3));
        assertThat(created.goalCode()).isEqualTo("LOSE_WEIGHT");
        assertThat(created.userId()).isEqualTo(id);

        PreferencesResponse replaced = preferences.save(id, request("BUILD_MUSCLE", 5));
        assertThat(replaced.goalCode()).isEqualTo("BUILD_MUSCLE");
        assertThat(preferences.get(id).daysPerWeek()).isEqualTo(5); // one row, updated, not a second insert
    }

    @Test
    void unknownCatalogCodeIsRejectedWithTheFieldName() {
        Long id = newUserId();

        assertThatThrownBy(() -> preferences.save(id, request("FLY_TO_MARS", 3)))
                .isInstanceOfSatisfying(UnknownCodeException.class, e -> assertThat(e.getField()).isEqualTo("goalCode"));
    }

    @Test
    void unknownUserAndMissingPreferencesAreNotFound() {
        assertThatThrownBy(() -> preferences.save(9999L, request("LOSE_WEIGHT", 3))).isInstanceOf(NotFoundException.class);
        Long id = newUserId();
        assertThatThrownBy(() -> preferences.get(id)).isInstanceOf(NotFoundException.class);
    }
}
