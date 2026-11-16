package com.wellness.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "diet_type")
public class DietType {

    @Id
    @Column(length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    protected DietType() {}

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof DietType d && code != null && code.equals(d.code));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
