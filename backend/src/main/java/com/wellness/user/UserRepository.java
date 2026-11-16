package com.wellness.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    /** Derived query: Spring Data builds the SQL from the method name. */
    boolean existsByEmail(String email);
}
