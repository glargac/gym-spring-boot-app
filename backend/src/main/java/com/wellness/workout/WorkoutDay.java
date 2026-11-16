package com.wellness.workout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_day")
public class WorkoutDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private WorkoutTemplate template;

    @Column(name = "day_index", nullable = false)
    private int dayIndex;

    @Column(nullable = false, length = 120)
    private String focus;

    @OneToMany(mappedBy = "day")
    @OrderBy("id") // without an explicit order the DB may return rows in any order
    private List<WorkoutExercise> exercises = new ArrayList<>();

    protected WorkoutDay() {}

    public Long getId() { return id; }
    public WorkoutTemplate getTemplate() { return template; }
    public int getDayIndex() { return dayIndex; }
    public String getFocus() { return focus; }
    public List<WorkoutExercise> getExercises() { return exercises; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof WorkoutDay d && id != null && id.equals(d.id));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
