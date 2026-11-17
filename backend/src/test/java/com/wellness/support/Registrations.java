package com.wellness.support;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import com.wellness.user.RegisterUserRequest;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TEST DATA BUILDER. Tests used to repeat an 8-argument constructor, so a reader could not tell which value mattered.
 * Now every test starts from a valid default and names only what it cares about:
 * <pre>aRegistration().email("a@b.com").sex(Sex.FEMALE).build()</pre>
 * Compare with an "Object Mother" (static factories such as anAdultUser()): a mother has one method per variant and
 * grows without end, a builder composes variants.
 */
public final class Registrations {

    private String email = "user@example.com";
    private String password = "s3cret-pass";
    private String fullName = "Test User";
    private LocalDate birthDate = LocalDate.of(1990, 1, 1);
    private Sex sex = Sex.MALE;
    private int heightCm = 178;
    private BigDecimal weightKg = new BigDecimal("80");
    private ActivityLevel activityLevel = ActivityLevel.MODERATE;

    private Registrations() {}

    public static Registrations aRegistration() {
        return new Registrations();
    }

    public Registrations email(String email) { this.email = email; return this; }
    public Registrations fullName(String fullName) { this.fullName = fullName; return this; }
    public Registrations birthDate(LocalDate birthDate) { this.birthDate = birthDate; return this; }
    public Registrations sex(Sex sex) { this.sex = sex; return this; }
    public Registrations heightCm(int heightCm) { this.heightCm = heightCm; return this; }
    public Registrations weightKg(String weightKg) { this.weightKg = new BigDecimal(weightKg); return this; }
    public Registrations activityLevel(ActivityLevel activityLevel) { this.activityLevel = activityLevel; return this; }

    public RegisterUserRequest build() {
        return new RegisterUserRequest(email, password, fullName, birthDate, sex, heightCm, weightKg, activityLevel);
    }
}
