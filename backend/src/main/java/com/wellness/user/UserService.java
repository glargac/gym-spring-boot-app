package com.wellness.user;

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

        AppUser user = new AppUser(email, encoder.encode(r.password()), Role.USER, r.fullName().trim(),
                r.birthDate(), r.sex(), r.heightCm(), r.weightKg(), r.activityLevel());
        try {
            // flush now so a UNIQUE violation surfaces here, not later at commit time outside this try block
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // check-then-insert is a race: two concurrent requests can both pass existsByEmail. The DB constraint is the real guard.
            throw new EmailAlreadyUsedException(email);
        }
    }
}
