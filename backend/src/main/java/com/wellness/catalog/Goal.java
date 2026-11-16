package com.wellness.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Reference data managed by the admin. Natural key (code) instead of a surrogate id. */
@Entity
@Table(name = "goal")
public class Goal {

    @Id
    @Column(length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    protected Goal() {} // required by JPA, hidden from application code

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    // equals/hashCode on the id only: entity identity must not depend on mutable fields.
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Goal g && code != null && code.equals(g.code));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); } // stable across the persist() id assignment
}
