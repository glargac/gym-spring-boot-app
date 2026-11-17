package com.wellness.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Tiny HTTP client for tests that hit a real server on a random port. JSON is read with regexes (no JSON library needed). */
public class TestHttp {

    private final HttpClient client = HttpClient.newHttpClient();
    private final int port;

    public TestHttp(int port) {
        this.port = port;
    }

    public HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** First occurrence of "name":value in a JSON string, as text. */
    public static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":\"?([^\",}\\]]+)").matcher(json);
        assertThat(m.find()).as("field %s in %s", name, json).isTrue();
        return m.group(1);
    }

    public String login(String email, String password) throws Exception {
        var r = call("POST", "/api/auth/login", null, "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
        assertThat(r.statusCode()).as("login %s: %s", email, r.body()).isEqualTo(200);
        return field(r.body(), "accessToken");
    }

    /** Registers a fresh user (unique email per call) and returns {id, email}. Password is always "s3cret-pass". */
    public String[] register() throws Exception {
        String email = "u-" + UUID.randomUUID() + "@example.com";
        var r = call("POST", "/api/users", null, "{\"email\":\"" + email + "\",\"password\":\"s3cret-pass\","
                + "\"fullName\":\"Test\",\"birthDate\":\"1990-01-01\",\"sex\":\"MALE\",\"heightCm\":180,"
                + "\"weightKg\":80,\"activityLevel\":\"MODERATE\"}");
        assertThat(r.statusCode()).isEqualTo(201);
        return new String[] {field(r.body(), "id"), email};
    }
}
