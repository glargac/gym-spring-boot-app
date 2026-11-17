package com.wellness.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 401 and 403 are produced by servlet FILTERS, before Spring MVC runs, so @RestControllerAdvice never sees them.
 * These two handlers give them the same RFC 9457 problem+json shape as every other error in the API.
 */
@Component
public class JsonSecurityErrors implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override // not authenticated: no token, bad signature, expired...
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer"); // required by RFC 6750 for bearer-token APIs
        write(request, response, 401, "Unauthorized", "Missing, invalid or expired access token");
    }

    @Override // authenticated, but not allowed
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(request, response, 403, "Forbidden", "You are not allowed to access this resource");
    }

    private static void write(HttpServletRequest request, HttpServletResponse response, int status, String title,
                              String detail) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Built by hand to avoid depending on a specific Jackson version; the only dynamic value is the path, so it is escaped.
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + title + "\",\"status\":" + status
                + ",\"detail\":\"" + detail + "\",\"instance\":\"" + escape(request.getRequestURI()) + "\"}");
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c < 0x20) {
                out.append(String.format("\\u%04x", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
