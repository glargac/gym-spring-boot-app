package com.wellness.admin;

import com.wellness.admin.AdminViews.MealPlanView;
import com.wellness.catalog.DietTypeRepository;
import com.wellness.catalog.GoalRepository;
import com.wellness.meal.Meal;
import com.wellness.meal.MealPlan;
import com.wellness.meal.MealPlanRepository;
import com.wellness.plan.RecommendationResponse.MealView;
import com.wellness.web.NotFoundException;
import com.wellness.web.UnknownCodeException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMealPlanService {

    private final MealPlanRepository plans;
    private final GoalRepository goals;
    private final DietTypeRepository dietTypes;

    public AdminMealPlanService(MealPlanRepository plans, GoalRepository goals, DietTypeRepository dietTypes) {
        this.plans = plans;
        this.goals = goals;
        this.dietTypes = dietTypes;
    }

    @Transactional(readOnly = true)
    public List<MealPlanView> list() {
        return plans.findAll(Sort.by("id")).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public MealPlanView get(Long id) {
        return toView(find(id));
    }

    @Transactional
    public MealPlanView create(MealPlanRequest r) {
        var plan = new MealPlan(goal(r), diet(r), r.kcalAdjust(), r.name().trim());
        fillMeals(plan, r);
        return toView(plans.saveAndFlush(plan));
    }

    @Transactional
    public MealPlanView update(Long id, MealPlanRequest r) {
        MealPlan plan = find(id);
        plan.update(goal(r), diet(r), r.kcalAdjust(), r.name().trim());
        plan.clearMeals();
        fillMeals(plan, r);
        return toView(plans.saveAndFlush(plan));
    }

    @Transactional
    public void delete(Long id) {
        plans.delete(find(id));
    }

    private MealPlan find(Long id) {
        return plans.findById(id).orElseThrow(() -> new NotFoundException("Meal plan " + id + " not found"));
    }

    private com.wellness.catalog.Goal goal(MealPlanRequest r) {
        return goals.findById(r.goalCode()).orElseThrow(() -> new UnknownCodeException("goalCode", r.goalCode()));
    }

    private com.wellness.catalog.DietType diet(MealPlanRequest r) {
        return dietTypes.findById(r.dietTypeCode()).orElseThrow(() -> new UnknownCodeException("dietTypeCode", r.dietTypeCode()));
    }

    private static void fillMeals(MealPlan plan, MealPlanRequest r) {
        r.meals().forEach(m -> plan.addMeal(new Meal(plan, m.mealType(), m.description().trim(), m.kcal())));
    }

    private MealPlanView toView(MealPlan p) {
        List<MealView> meals = p.getMeals().stream()
                .map(m -> new MealView(m.getMealType().name(), m.getDescription(), m.getKcal())).toList();
        return new MealPlanView(p.getId(), p.getGoal().getCode(), p.getDietType().getCode(), p.getKcalAdjust(), p.getName(), meals);
    }
}
