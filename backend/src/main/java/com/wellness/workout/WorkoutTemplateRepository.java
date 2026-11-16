package com.wellness.workout;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    // "GoalCode" is resolved as goal.code: Spring Data walks the property path through the association.
    List<WorkoutTemplate> findByGoalCodeAndLocationCodeOrderById(String goalCode, String locationCode);

    List<WorkoutTemplate> findByGoalCodeOrderById(String goalCode);
}
