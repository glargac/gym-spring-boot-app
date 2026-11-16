package com.wellness.meal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan mealPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private int kcal;

    protected Meal() {}

    public Long getId() { return id; }
    public MealPlan getMealPlan() { return mealPlan; }
    public MealType getMealType() { return mealType; }
    public String getDescription() { return description; }
    public int getKcal() { return kcal; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Meal m && id != null && id.equals(m.id));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
