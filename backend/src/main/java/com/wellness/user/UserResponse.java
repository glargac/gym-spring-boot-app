package com.wellness.user;

/** What the API returns about a user. No passwordHash, ever. */
public record UserResponse(Long id, String email, String fullName, Role role) {

    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole());
    }
}
