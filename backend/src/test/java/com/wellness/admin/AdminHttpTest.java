package com.wellness.admin;

import static com.wellness.support.TestHttp.field;
import static org.assertj.core.api.Assertions.assertThat;

import com.wellness.support.TestHttp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:admintest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "app.admin.initial-password=admin-test-pass",
        "app.jwt.secret=test-secret-test-secret-test-secret-123456"})
class AdminHttpTest {

    @LocalServerPort int port;
    TestHttp http;
    String admin;

    @BeforeEach
    void setUp() throws Exception {
        http = new TestHttp(port);
        admin = http.login("admin@wellness.local", "admin-test-pass");
    }

    private static final String TEMPLATE = "{\"goalCode\":\"MAINTAIN\",\"locationCode\":\"HOME\",\"daysPerWeek\":2,"
            + "\"name\":\"Home basics\",\"days\":["
            + "{\"dayIndex\":1,\"focus\":\"Legs\",\"exercises\":[{\"name\":\"Squat\",\"sets\":3,\"reps\":\"12\"}]},"
            + "{\"dayIndex\":2,\"focus\":\"Upper\",\"exercises\":[{\"name\":\"Push-up\",\"sets\":3,\"reps\":\"10\"}]}]}";

    private static final String MEAL_PLAN = "{\"goalCode\":\"MAINTAIN\",\"dietTypeCode\":\"VEGAN\",\"kcalAdjust\":0,"
            + "\"name\":\"Vegan basics\",\"meals\":["
            + "{\"mealType\":\"BREAKFAST\",\"description\":\"Oats and fruit\",\"kcal\":500},"
            + "{\"mealType\":\"LUNCH\",\"description\":\"Lentil bowl\",\"kcal\":700}]}";

    @Test
    void adminEndpointsAreClosedToUsersAndAnonymous() throws Exception {
        var user = http.register();
        String userToken = http.login(user[1], "s3cret-pass");

        assertThat(http.call("GET", "/api/admin/workout-templates", null, null).statusCode()).isEqualTo(401);
        var forbidden = http.call("GET", "/api/admin/workout-templates", userToken, null);
        assertThat(forbidden.statusCode()).isEqualTo(403);
        assertThat(http.call("PUT", "/api/admin/goals/HACK", userToken, "{\"name\":\"x\"}").statusCode()).isEqualTo(403);
    }

    @Test
    void adminManagesACatalogItem() throws Exception {
        var created = http.call("PUT", "/api/admin/goals/TEST_GOAL", admin, "{\"name\":\"Test goal\",\"description\":\"d\"}");
        assertThat(created.statusCode()).isEqualTo(200);
        assertThat(http.call("GET", "/api/goals", null, null).body()).contains("TEST_GOAL");

        var renamed = http.call("PUT", "/api/admin/goals/TEST_GOAL", admin, "{\"name\":\"Renamed\"}");
        assertThat(field(renamed.body(), "name")).isEqualTo("Renamed");

        assertThat(http.call("PUT", "/api/admin/goals/bad%20code", admin, "{\"name\":\"x\"}").statusCode()).isEqualTo(400);
        assertThat(http.call("PUT", "/api/admin/goals/TEST_GOAL", admin, "{\"name\":\"\"}").statusCode()).isEqualTo(400);

        assertThat(http.call("DELETE", "/api/admin/goals/TEST_GOAL", admin, null).statusCode()).isEqualTo(204);
        assertThat(http.call("DELETE", "/api/admin/goals/TEST_GOAL", admin, null).statusCode()).isEqualTo(404);
    }

    @Test
    void aCatalogItemStillInUseCannotBeDeleted() throws Exception {
        var r = http.call("DELETE", "/api/admin/goals/LOSE_WEIGHT", admin, null);

        assertThat(r.statusCode()).isEqualTo(409);
        assertThat(r.body()).contains("still in use");
        assertThat(http.call("GET", "/api/goals", null, null).body()).contains("LOSE_WEIGHT");
    }

    @Test
    void adminManagesAWorkoutTemplate() throws Exception {
        var created = http.call("POST", "/api/admin/workout-templates", admin, TEMPLATE);
        assertThat(created.statusCode()).isEqualTo(201);
        String id = field(created.body(), "id");

        var read = http.call("GET", "/api/admin/workout-templates/" + id, admin, null);
        assertThat(read.body()).contains("Home basics", "Squat", "Push-up");

        // replace: one fewer exercise name and a new template name
        var updated = http.call("PUT", "/api/admin/workout-templates/" + id, admin,
                TEMPLATE.replace("Home basics", "Home basics v2").replace("Squat", "Lunge"));
        assertThat(updated.statusCode()).isEqualTo(200);
        var reread = http.call("GET", "/api/admin/workout-templates/" + id, admin, null).body();
        assertThat(reread).contains("Home basics v2", "Lunge").doesNotContain("Squat");

        assertThat(http.call("DELETE", "/api/admin/workout-templates/" + id, admin, null).statusCode()).isEqualTo(204);
        assertThat(http.call("GET", "/api/admin/workout-templates/" + id, admin, null).statusCode()).isEqualTo(404);
    }

    @Test
    void workoutTemplateRulesAreEnforced() throws Exception {
        // 3 days promised but only 2 sent
        var mismatch = http.call("POST", "/api/admin/workout-templates", admin, TEMPLATE.replace("\"daysPerWeek\":2", "\"daysPerWeek\":3"));
        assertThat(mismatch.statusCode()).isEqualTo(400);
        assertThat(mismatch.body()).contains("\"days\"");

        var unknownGoal = http.call("POST", "/api/admin/workout-templates", admin, TEMPLATE.replace("MAINTAIN", "NOPE"));
        assertThat(unknownGoal.statusCode()).isEqualTo(400);
        assertThat(unknownGoal.body()).contains("goalCode");

        // nested validation: a blank exercise name is reported with its path
        var nested = http.call("POST", "/api/admin/workout-templates", admin, TEMPLATE.replace("\"Squat\"", "\"\""));
        assertThat(nested.statusCode()).isEqualTo(400);
        assertThat(nested.body()).contains("days[0].exercises[0].name");
    }

    @Test
    void adminManagesAMealPlan() throws Exception {
        var created = http.call("POST", "/api/admin/meal-plans", admin, MEAL_PLAN);
        assertThat(created.statusCode()).isEqualTo(201);
        String id = field(created.body(), "id");

        var updated = http.call("PUT", "/api/admin/meal-plans/" + id, admin, MEAL_PLAN.replace("Lentil bowl", "Chickpea bowl"));
        assertThat(updated.body()).contains("Chickpea bowl").doesNotContain("Lentil bowl");

        assertThat(http.call("POST", "/api/admin/meal-plans", admin, MEAL_PLAN.replace("\"mealType\":\"LUNCH\"", "\"mealType\":\"BRUNCH\""))
                .statusCode()).isEqualTo(400);
        assertThat(http.call("POST", "/api/admin/meal-plans", admin,
                "{\"goalCode\":\"MAINTAIN\",\"dietTypeCode\":\"VEGAN\",\"kcalAdjust\":0,\"name\":\"Empty\",\"meals\":[]}")
                .statusCode()).isEqualTo(400);

        assertThat(http.call("DELETE", "/api/admin/meal-plans/" + id, admin, null).statusCode()).isEqualTo(204);
        assertThat(http.call("GET", "/api/admin/meal-plans/" + id, admin, null).statusCode()).isEqualTo(404);
    }
}
