package com.wellness.meal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    List<MealPlan> findByGoalCodeAndDietTypeCodeOrderById(String goalCode, String dietTypeCode);
}
