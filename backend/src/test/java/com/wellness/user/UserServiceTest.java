package com.wellness.user;

import static com.wellness.support.Registrations.aRegistration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wellness.health.HealthMetricsService.ActivityLevel;
import com.wellness.health.HealthMetricsService.Sex;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional // each test rolls back, so tests don't leak users into each other
class UserServiceTest {

    @Autowired UserService service;
    @Autowired PasswordEncoder encoder;

    private RegisterUserRequest request(String email) {
        return aRegistration().email(email).fullName("Ana Lopez").birthDate(LocalDate.of(1992, 3, 14))
                .sex(Sex.FEMALE).heightCm(165).weightKg("60.5").activityLevel(ActivityLevel.LIGHT).build();
    }

    @Test
    void registersAUserWithHashedPasswordAndUserRole() {
        AppUser saved = service.register(request("  Ana@Example.COM "));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("ana@example.com"); // trimmed + lower-cased
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getPasswordHash()).isNotEqualTo("s3cret-pass");
        assertThat(encoder.matches("s3cret-pass", saved.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.register(request("ana@example.com"));

        assertThatThrownBy(() -> service.register(request("ANA@example.com")))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void requestToStringMasksThePassword() {
        assertThat(request("ana@example.com").toString()).doesNotContain("s3cret-pass");
    }
}
