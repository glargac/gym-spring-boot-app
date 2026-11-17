package com.wellness.admin;

import com.wellness.admin.AdminViews.MealPlanView;
import com.wellness.catalog.DietType;
import com.wellness.catalog.DietTypeRepository;
import com.wellness.catalog.Goal;
import com.wellness.catalog.GoalRepository;
import com.wellness.meal.Meal;
import com.wellness.meal.MealPlan;
import com.wellness.meal.MealPlanRepository;
import com.wellness.plan.RecommendationResponse.MealView;
import com.wellness.web.UnknownCodeException;
import java.util.List;
import org.springframework.stereotype.Service;

/** Only what is specific to meal plans: the generic CRUD flow lives in {@link AdminPlanService}. No extra rules, so no validate() hook. */
@Service
public class AdminMealPlanService extends AdminPlanService<MealPlan, MealPlanRequest, MealPlanView> {

    private final GoalRepository goals;
    private final DietTypeRepository dietTypes;

    public AdminMealPlanService(MealPlanRepository plans, GoalRepository goals, DietTypeRepository dietTypes) {
        super(plans, "Meal plan");
        this.goals = goals;
        this.dietTypes = dietTypes;
    }

    @Override
    protected MealPlan newEntity(MealPlanRequest r) {
        var plan = new MealPlan(goal(r), diet(r), r.kcalAdjust(), r.name().trim());
        fillMeals(plan, r);
        return plan;
    }

    @Override
    protected void replaceState(MealPlan plan, MealPlanRequest r) {
        plan.update(goal(r), diet(r), r.kcalAdjust(), r.name().trim());
        plan.clearMeals();
        fillMeals(plan, r);
    }

    @Override
    protected MealPlanView toView(MealPlan p) {
        List<MealView> meals = p.getMeals().stream()
                .map(m -> new MealView(m.getMealType().name(), m.getDescription(), m.getKcal())).toList();
        return new MealPlanView(p.getId(), p.getGoal().getCode(), p.getDietType().getCode(), p.getKcalAdjust(), p.getName(), meals);
    }

    private Goal goal(MealPlanRequest r) {
        return goals.findById(r.goalCode()).orElseThrow(() -> new UnknownCodeException("goalCode", r.goalCode()));
    }

    private DietType diet(MealPlanRequest r) {
        return dietTypes.findById(r.dietTypeCode()).orElseThrow(() -> new UnknownCodeException("dietTypeCode", r.dietTypeCode()));
    }

    private static void fillMeals(MealPlan plan, MealPlanRequest r) {
        r.meals().forEach(m -> plan.addMeal(new Meal(plan, m.mealType(), m.description().trim(), m.kcal())));
    }
}
