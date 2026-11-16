package com.wellness.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.wellness.health.HealthMetrics.BmiCategory;
import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HealthMetricsServiceTest {

    private final HealthMetricsService service = new HealthMetricsService();

    @Test
    void man40_185cm_85kg_isNormalButAtUpperLimit() {
        var r = service.calculate(Sex.MALE, LocalDate.now().minusYears(40), 185, 85, ActivityLevel.MODERATE);

        assertThat(r.bmi()).isEqualTo(24.8);                      // 85 / 1.85²
        assertThat(r.category()).isEqualTo(BmiCategory.NORMAL);   // < 25
        assertThat(r.bmr()).isEqualTo(1811.3);                    // 850 + 1156.25 - 200 + 5
    }
}
