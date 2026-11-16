package com.wellness.security;

import com.wellness.user.UserRepository;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * V2 seeds the admin with a placeholder hash (a real hash can't live in a migration: it would be committed to git).
 * At startup we give that account a real password from configuration, or a random one for local dev.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@wellness.local";
    private static final String PLACEHOLDER_PREFIX = "PLACEHOLDER";
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AdminProperties props;

    public AdminBootstrap(UserRepository users, PasswordEncoder encoder, AdminProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        users.findByEmail(ADMIN_EMAIL).ifPresent(admin -> {
            String configured = props.initialPassword();
            boolean hasConfigured = configured != null && !configured.isBlank();

            if (hasConfigured) {
                if (!encoder.matches(configured, admin.getPasswordHash())) { // skip a pointless re-hash on every start
                    admin.changePasswordHash(encoder.encode(configured));
                    users.save(admin);
                    log.info("Admin password set from configuration");
                }
            } else if (admin.getPasswordHash().startsWith(PLACEHOLDER_PREFIX)) {
                byte[] bytes = new byte[18];
                new SecureRandom().nextBytes(bytes);
                String generated = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                admin.changePasswordHash(encoder.encode(generated));
                users.save(admin);
                // Same approach as Spring Boot's own "generated security password": acceptable for local dev only.
                log.warn("Generated admin password for {} (dev only; set WELLNESS_ADMIN_PASSWORD to choose one): {}",
                        ADMIN_EMAIL, generated);
            }
        });
    }
}
