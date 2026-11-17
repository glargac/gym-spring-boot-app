package com.wellness.user;

import com.wellness.web.DbErrors;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public AppUser register(RegisterUserRequest r) {
        // Emails are case-insensitive in practice, but the DB UNIQUE is case-sensitive: normalise before checking and saving.
        String email = r.email().trim().toLowerCase(Locale.ROOT);

        if (users.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(encoder.encode(r.password()))
                .role(Role.USER) // explicit on purpose: registration never creates an admin
                .fullName(r.fullName().trim())
                .birthDate(r.birthDate())
                .sex(r.sex())
                .heightCm(r.heightCm())
                .weightKg(r.weightKg())
                .activityLevel(r.activityLevel())
                .build();
        try {
            // flush now so a UNIQUE violation surfaces here, not later at commit time outside this try block
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // check-then-insert is a race: two concurrent requests can both pass existsByEmail. The DB constraint is the real guard.
            // Only a UNIQUE violation means "email taken". Any other integrity error is a bug or a schema mismatch,
            // so it must surface as itself instead of being disguised as a duplicate email.
            if (DbErrors.isUniqueViolation(e)) {
                throw new EmailAlreadyUsedException(email);
            }
            throw e;
        }
    }
}
