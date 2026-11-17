package com.wellness.security;

import static com.wellness.support.TestHttp.field;
import static org.assertj.core.api.Assertions.assertThat;

import com.wellness.support.TestHttp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Real HTTP against a real server on a random port, so the whole filter chain runs. It uses its own in-memory
 * database: it commits data, unlike the @Transactional tests that roll back.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sectest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "app.admin.initial-password=admin-test-pass",
        "app.jwt.secret=test-secret-test-secret-test-secret-123456"})
class SecurityHttpTest {

    @LocalServerPort int port;
    TestHttp http;

    @BeforeEach
    void setUp() {
        http = new TestHttp(port);
    }

    @Test
    void publicEndpointsNeedNoToken() throws Exception {
        assertThat(http.call("GET", "/api/goals", null, null).statusCode()).isEqualTo(200);
        assertThat(http.call("GET", "/api/diet-types", null, null).statusCode()).isEqualTo(200);
    }

    @Test
    void protectedEndpointRejectsMissingAndGarbageTokens() throws Exception {
        var noToken = http.call("GET", "/api/users/1/preferences", null, null);
        assertThat(noToken.statusCode()).isEqualTo(401);
        assertThat(noToken.body()).contains("\"status\":401"); // JSON problem body, same shape as the other errors
        assertThat(noToken.headers().firstValue("WWW-Authenticate")).hasValue("Bearer");
        assertThat(http.call("GET", "/api/users/1/preferences", "not.a.jwt", null).statusCode()).isEqualTo(401);
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameAnswer() throws Exception {
        var user = http.register();
        var wrongPassword = http.call("POST", "/api/auth/login", null,
                "{\"email\":\"" + user[1] + "\",\"password\":\"wrong-password\"}");
        var unknownEmail = http.call("POST", "/api/auth/login", null,
                "{\"email\":\"nobody@example.com\",\"password\":\"wrong-password\"}");

        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(unknownEmail.statusCode()).isEqualTo(401);
        assertThat(field(wrongPassword.body(), "detail")).isEqualTo(field(unknownEmail.body(), "detail"));
    }

    @Test
    void userCanReachOwnDataButNotSomeoneElses() throws Exception {
        var a = http.register();
        var b = http.register();
        String tokenA = http.login(a[1], "s3cret-pass");

        // 404 = authorised, there just are no preferences yet
        assertThat(http.call("GET", "/api/users/" + a[0] + "/preferences", tokenA, null).statusCode()).isEqualTo(404);
        var forbidden = http.call("GET", "/api/users/" + b[0] + "/preferences", tokenA, null);
        assertThat(forbidden.statusCode()).isEqualTo(403);
        assertThat(forbidden.body()).contains("\"status\":403");
        assertThat(http.call("GET", "/api/users/" + b[0] + "/recommendation", tokenA, null).statusCode()).isEqualTo(403);
    }

    @Test
    void adminCanReachAnyUsersData() throws Exception {
        var user = http.register();
        String adminToken = http.login("admin@wellness.local", "admin-test-pass");

        assertThat(http.call("GET", "/api/users/" + user[0] + "/preferences", adminToken, null).statusCode()).isEqualTo(404);
    }

    @Test
    void corsPreflightIsAnsweredWithoutATokenForTheAllowedOriginOnly() throws Exception {
        var allowed = http.call("OPTIONS", "/api/users/1/preferences", null, null,
                "Origin", "http://localhost:5173", "Access-Control-Request-Method", "PUT",
                "Access-Control-Request-Headers", "authorization,content-type");
        assertThat(allowed.statusCode()).isEqualTo(200);
        assertThat(allowed.headers().firstValue("Access-Control-Allow-Origin")).hasValue("http://localhost:5173");

        var other = http.call("OPTIONS", "/api/users/1/preferences", null, null,
                "Origin", "http://evil.example", "Access-Control-Request-Method", "PUT");
        assertThat(other.statusCode()).isEqualTo(403);
        assertThat(other.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }

    @Test
    void aUserTokenCannotOpenTheAdminAccount() throws Exception {
        var user = http.register();
        String token = http.login(user[1], "s3cret-pass");

        assertThat(http.call("GET", "/api/users/1/preferences", token, null).statusCode()).isEqualTo(403);
    }
}
