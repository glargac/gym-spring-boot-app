package com.wellness.user;

import org.springframework.data.jpa.repository.JpaRepository;

/** The id type is Long because the primary key is the user's id (shared primary key via @MapsId). */
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
}
