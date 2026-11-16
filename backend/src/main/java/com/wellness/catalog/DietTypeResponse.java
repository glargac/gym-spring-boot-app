package com.wellness.catalog;

public record DietTypeResponse(String code, String name, String description) {

    public static DietTypeResponse from(DietType d) {
        return new DietTypeResponse(d.getCode(), d.getName(), d.getDescription());
    }
}
