package com.wellness.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.wellness.workout.WorkoutTemplate;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Reads the Flyway-seeded tables through the JPA entities. Full context with the configured H2. */
@SpringBootTest
@Transactional // keeps a session open so lazy collections can load; rolled back after each test
class SeedDataJpaTest {

    @Autowired
    EntityManager em;

    @Test
    void goalsAreSeeded() {
        var goals = em.createQuery("select g from Goal g order by g.code", Goal.class).getResultList();
        goals.forEach(g -> System.out.println("GOAL -> " + g.getCode() + " | " + g.getName()));
        assertThat(goals).extracting(Goal::getCode)
                .contains("LOSE_WEIGHT", "GAIN_WEIGHT", "BUILD_MUSCLE", "BURN_FAT", "MAINTAIN");
    }

    @Test
    void workoutTemplateLoadsItsDays() {
        var templates = em.createQuery("select t from WorkoutTemplate t", WorkoutTemplate.class).getResultList();
        assertThat(templates).isNotEmpty();
        // lazy collection: works because the test is @Transactional (outside one it would throw LazyInitializationException)
        assertThat(templates.get(0).getDays()).hasSize(3);
    }
}
