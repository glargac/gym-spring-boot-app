package com.wellness.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data generates the implementation at startup (a JDK dynamic proxy), so no SQL or boilerplate is written here. */
public interface GoalRepository extends JpaRepository<Goal, String> {
}
