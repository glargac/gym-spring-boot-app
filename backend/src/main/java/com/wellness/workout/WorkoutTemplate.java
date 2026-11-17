package com.wellness.workout;

import com.wellness.catalog.Goal;
import com.wellness.catalog.TrainingLocation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import org.hibernate.annotations.BatchSize;
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
@Table(name = "workout_template")
public class WorkoutTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_code")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_code")
    private TrainingLocation location;

    @Column(name = "days_per_week", nullable = false)
    private int daysPerWeek;

    @Column(nullable = false, length = 120)
    private String name;

    // mappedBy = the FK lives in workout_day; this side is not the owner. Lazy by default (N+1 risk when iterated in a loop).
    // cascade ALL + orphanRemoval: the template owns its days (an aggregate). Saving/deleting the root saves/deletes
    // the children, and removing a day from the list deletes its row.
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayIndex")
    @BatchSize(size = 50) // loads the days of up to 50 templates in one query instead of one query per template
    private List<WorkoutDay> days = new ArrayList<>();

    protected WorkoutTemplate() {}

    public WorkoutTemplate(Goal goal, TrainingLocation location, int daysPerWeek, String name) {
        update(goal, location, daysPerWeek, name);
    }

    public void update(Goal goal, TrainingLocation location, int daysPerWeek, String name) {
        this.goal = goal;
        this.location = location;
        this.daysPerWeek = daysPerWeek;
        this.name = name;
    }

    /** Keeps both sides of the bidirectional association in sync (the day's FK is what gets written to the DB). */
    public void addDay(WorkoutDay day) {
        days.add(day);
    }

    public void clearDays() {
        days.clear(); // orphanRemoval deletes the removed days (and, by cascade, their exercises)
    }

    public Long getId() { return id; }
    public Goal getGoal() { return goal; }
    public TrainingLocation getLocation() { return location; }
    public int getDaysPerWeek() { return daysPerWeek; }
    public String getName() { return name; }
    public List<WorkoutDay> getDays() { return days; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof WorkoutTemplate t && id != null && id.equals(t.id));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
