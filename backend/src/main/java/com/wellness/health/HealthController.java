package com.wellness.health;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthMetricsService service; // constructor injection (DIP, easy to test)

    public HealthController(HealthMetricsService service) {
        this.service = service;
    }

    /** Input DTO as a record + Bean Validation. */
    public record AssessmentRequest(
            @NotNull Sex sex,
            @NotNull @Past LocalDate birthDate,
            @Min(100) @Max(250) int heightCm,
            @Min(30) @Max(300) double weightKg,
            @NotNull ActivityLevel activityLevel) {}

    @PostMapping("/assessment")
    public HealthMetrics assess(@Valid @RequestBody AssessmentRequest r) {
        return service.calculate(r.sex(), r.birthDate(), r.heightCm(), r.weightKg(), r.activityLevel());
    }
}
