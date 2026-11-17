package com.wellness.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** A plain unit test: no Spring, no database. Entities with a builder can be tested in milliseconds. */
class AppUserBuilderTest {

    private AppUser.Builder complete() {
        return AppUser.builder().email("a@b.com").passwordHash("hash").fullName("Ana")
                .birthDate(LocalDate.of(1990, 1, 1)).sex(Sex.FEMALE).heightCm(165)
                .weightKg(new BigDecimal("60")).activityLevel(ActivityLevel.LIGHT);
    }

    @Test
    void roleDefaultsToUserSoAdminMustBeAskedForExplicitly() {
        assertThat(complete().build().getRole()).isEqualTo(Role.USER);
        assertThat(complete().role(Role.ADMIN).build().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void aUserCanNeverBeHalfBuilt() {
        assertThatThrownBy(() -> AppUser.builder().email("a@b.com").build())
                .isInstanceOf(NullPointerException.class).hasMessageContaining("passwordHash");
        assertThatThrownBy(() -> complete().email(null).build())
                .isInstanceOf(NullPointerException.class).hasMessageContaining("email");
        assertThatThrownBy(() -> complete().heightCm(0).build())
                .isInstanceOf(IllegalStateException.class);
    }
}
