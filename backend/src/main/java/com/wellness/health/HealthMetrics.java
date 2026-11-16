package com.wellness.health;

/** Result of the health calculation. A record: immutable, equals/hashCode/toString for free (Java 16+). */
public record HealthMetrics(double bmi, BmiCategory category, double bmr, double tdee, String advice) {

    /** WHO categories. An enum with behavior keeps the thresholds in one place instead of scattered if/else. */
    public enum BmiCategory {
        UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE;

        public static BmiCategory of(double bmi) {
            if (bmi < 18.5) return UNDERWEIGHT;
            if (bmi < 25) return NORMAL;
            if (bmi < 30) return OVERWEIGHT;
            return OBESE;
        }
    }
}
