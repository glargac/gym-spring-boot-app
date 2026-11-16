package com.wellness.workout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "workout_exercise")
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_id")
    private WorkoutDay day;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "sets", nullable = false)
    private int sets;

    // VARCHAR on purpose: values like "10", "20 min", "30 s/30 s" (a design smell: a value that mixes number and unit).
    @Column(nullable = false, length = 20)
    private String reps;

    protected WorkoutExercise() {}

    public Long getId() { return id; }
    public WorkoutDay getDay() { return day; }
    public String getName() { return name; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof WorkoutExercise e && id != null && id.equals(e.id));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
