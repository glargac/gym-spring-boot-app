package com.wellness.meal;

import com.wellness.catalog.DietType;
import com.wellness.catalog.Goal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plan")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_code")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diet_type_code")
    private DietType dietType;

    @Column(name = "kcal_adjust", nullable = false)
    private int kcalAdjust; // delta over TDEE, e.g. -500 to lose weight

    @Column(nullable = false, length = 120)
    private String name;

    @OneToMany(mappedBy = "mealPlan")
    private List<Meal> meals = new ArrayList<>();

    protected MealPlan() {}

    public Long getId() { return id; }
    public Goal getGoal() { return goal; }
    public DietType getDietType() { return dietType; }
    public int getKcalAdjust() { return kcalAdjust; }
    public String getName() { return name; }
    public List<Meal> getMeals() { return meals; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof MealPlan p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
