package com.wellness.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Calls the controllers as plain beans against the seeded H2 (no HTTP layer: Boot 4 moved MockMvc to a separate test starter). */
@SpringBootTest
class CatalogControllersTest {

    @Autowired GoalController goals;
    @Autowired DietTypeController dietTypes;
    @Autowired TrainingLocationController locations;

    @Test
    void goalsAreSortedByName() {
        assertThat(goals.list()).extracting(GoalResponse::name)
                .containsExactly("Build muscle", "Burn fat", "Gain weight", "Lose weight", "Maintain");
    }

    @Test
    void dietTypesAreSeeded() {
        assertThat(dietTypes.list()).extracting(DietTypeResponse::code)
                .containsExactlyInAnyOrder("STANDARD", "KETO", "VEGETARIAN", "VEGAN", "HIGH_PROTEIN", "MEDITERRANEAN");
    }

    @Test
    void trainingLocationsAreSeeded() {
        assertThat(locations.list()).extracting(TrainingLocationResponse::code)
                .containsExactlyInAnyOrder("GYM", "OUTDOOR", "HOME");
    }
}
