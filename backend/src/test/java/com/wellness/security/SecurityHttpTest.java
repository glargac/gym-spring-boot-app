package com.wellness.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Real HTTP against a real server on a random port, so the whole filter chain runs (JSON is read with regexes
 * to avoid depending on a JSON library). It uses its own in-memory database: it commits data, unlike the
 * @Transactional tests that roll back.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sectest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "app.admin.initial-password=admin-test-pass",
        "app.jwt.secret=test-secret-test-secret-test-secret-123456"})
class SecurityHttpTest {

    @LocalServerPort int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":\"?([^\",}]+)").matcher(json);
        assertThat(m.find()).as("field %s in %s", name, json).isTrue();
        return m.group(1);
    }

    private String login(String email, String password) throws Exception {
        var r = call("POST", "/api/auth/login", null, "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
        assertThat(r.statusCode()).isEqualTo(200);
        return field(r.body(), "accessToken");
    }

    /** Registers a fresh user (unique email per call) and returns {id, email}. */
    private String[] register() throws Exception {
        String email = "u-" + UUID.randomUUID() + "@example.com";
        var r = call("POST", "/api/users", null, "{\"email\":\"" + email + "\",\"password\":\"s3cret-pass\","
                + "\"fullName\":\"Test\",\"birthDate\":\"1990-01-01\",\"sex\":\"MALE\",\"heightCm\":180,"
                + "\"weightKg\":80,\"activityLevel\":\"MODERATE\"}");
        assertThat(r.statusCode()).isEqualTo(201);
        return new String[] {field(r.body(), "id"), email};
    }

    @Test
    void publicEndpointsNeedNoToken() throws Exception {
        assertThat(call("GET", "/api/goals", null, null).statusCode()).isEqualTo(200);
        assertThat(call("GET", "/api/diet-types", null, null).statusCode()).isEqualTo(200);
    }

    @Test
    void protectedEndpointRejectsMissingAndGarbageTokens() throws Exception {
        assertThat(call("GET", "/api/users/1/preferences", null, null).statusCode()).isEqualTo(401);
        assertThat(call("GET", "/api/users/1/preferences", "not.a.jwt", null).statusCode()).isEqualTo(401);
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameAnswer() throws Exception {
        var user = register();
        var wrongPassword = call("POST", "/api/auth/login", null,
                "{\"email\":\"" + user[1] + "\",\"password\":\"wrong-password\"}");
        var unknownEmail = call("POST", "/api/auth/login", null,
                "{\"email\":\"nobody@example.com\",\"password\":\"wrong-password\"}");

        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(unknownEmail.statusCode()).isEqualTo(401);
        assertThat(field(wrongPassword.body(), "detail")).isEqualTo(field(unknownEmail.body(), "detail"));
    }

    @Test
    void userCanReachOwnDataButNotSomeoneElses() throws Exception {
        var a = register();
        var b = register();
        String tokenA = login(a[1], "s3cret-pass");

        // 404 = authorised, there just are no preferences yet
        assertThat(call("GET", "/api/users/" + a[0] + "/preferences", tokenA, null).statusCode()).isEqualTo(404);
        assertThat(call("GET", "/api/users/" + b[0] + "/preferences", tokenA, null).statusCode()).isEqualTo(403);
        assertThat(call("GET", "/api/users/" + b[0] + "/recommendation", tokenA, null).statusCode()).isEqualTo(403);
    }

    @Test
    void adminCanReachAnyUsersData() throws Exception {
        var user = register();
        String adminToken = login("admin@wellness.local", "admin-test-pass");

        assertThat(call("GET", "/api/users/" + user[0] + "/preferences", adminToken, null).statusCode()).isEqualTo(404);
    }

    @Test
    void registrationCannotCreateAnAdmin() throws Exception {
        var user = register();
        String token = login(user[1], "s3cret-pass");

        // a USER token must not open an admin-only view of another account
        assertThat(call("GET", "/api/users/1/preferences", token, null).statusCode()).isEqualTo(403);
    }
}
