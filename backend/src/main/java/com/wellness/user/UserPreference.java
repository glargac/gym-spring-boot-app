package com.wellness.user;

import com.wellness.catalog.DietType;
import com.wellness.catalog.Goal;
import com.wellness.catalog.TrainingLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    // @MapsId: the PK is also the FK to app_user (shared primary key).
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    // @ManyToOne is EAGER by default; LAZY avoids loading three extra rows on every read.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_code")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_code")
    private TrainingLocation location;

    @Column(name = "days_per_week", nullable = false)
    private int daysPerWeek;

    @Column(name = "hours_per_day", nullable = false, precision = 3, scale = 1)
    private BigDecimal hoursPerDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diet_type_code")
    private DietType dietType;

    @Column(name = "excluded_foods", length = 255)
    private String excludedFoods;

    protected UserPreference() {}

    public Long getUserId() { return userId; }
    public AppUser getUser() { return user; }
    public Goal getGoal() { return goal; }
    public TrainingLocation getLocation() { return location; }
    public int getDaysPerWeek() { return daysPerWeek; }
    public BigDecimal getHoursPerDay() { return hoursPerDay; }
    public DietType getDietType() { return dietType; }
    public String getExcludedFoods() { return excludedFoods; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof UserPreference p && userId != null && userId.equals(p.userId));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
