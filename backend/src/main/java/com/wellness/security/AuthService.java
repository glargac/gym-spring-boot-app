package com.wellness.security;

import com.wellness.user.AppUser;
import com.wellness.user.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    /** Hash of a throwaway password, used so an unknown email costs the same time as a wrong password. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
        this.dummyHash = encoder.encode("not-a-real-password");
    }

    public TokenResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null);

        // Always run one BCrypt comparison. Returning early for unknown emails would be measurably faster
        // (BCrypt is deliberately slow), and that timing difference leaks which emails exist.
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean passwordOk = encoder.matches(request.password(), hash);

        if (user == null || !passwordOk) {
            throw new InvalidCredentialsException();
        }
        return tokens.issue(user);
    }
}
