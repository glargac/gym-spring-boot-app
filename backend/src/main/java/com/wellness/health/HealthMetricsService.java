package com.wellness.health;

import com.wellness.health.HealthMetrics.BmiCategory;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.stereotype.Service;

/** Calculates BMI, basal metabolic rate (Mifflin-St Jeor) and total daily energy expenditure. */
@Service
public class HealthMetricsService {

    public HealthMetrics calculate(Sex sex, LocalDate birthDate, int heightCm, double weightKg, ActivityLevel level) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        double heightM = heightCm / 100.0;

        double bmi = weightKg / (heightM * heightM);
        double sexConstant = switch (sex) {
            case MALE -> 5;
            case FEMALE -> -161;
            case OTHER -> -78; // average of the two
        };
        double bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + sexConstant;
        double tdee = bmr * level.factor();

        BmiCategory category = BmiCategory.of(bmi);
        return new HealthMetrics(round(bmi), category, round(bmr), round(tdee), adviceFor(category));
    }

    private String adviceFor(BmiCategory c) {
        return switch (c) {
            case UNDERWEIGHT -> "Consider a calorie surplus and strength training.";
            case NORMAL -> "Healthy weight. Keep your habits and work on body composition.";
            case OVERWEIGHT -> "Moderate deficit (~500 kcal) plus 3 exercise days per week.";
            case OBESE -> "Consult a professional; start with walking and a gradual deficit.";
        };
    }

    private static double round(double v) {
        return Math.round(v * 10) / 10.0;
    }

    public enum Sex { MALE, FEMALE, OTHER }

    public enum ActivityLevel {
        SEDENTARY(1.2), LIGHT(1.375), MODERATE(1.55), ACTIVE(1.725);

        private final double factor;
        ActivityLevel(double factor) { this.factor = factor; }
        public double factor() { return factor; }
    }
}
