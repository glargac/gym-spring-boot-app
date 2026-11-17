package com.wellness.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_location")
public class TrainingLocation {

    @Id
    @Column(length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    protected TrainingLocation() {}

    public TrainingLocation(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof TrainingLocation t && code != null && code.equals(t.code));
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
