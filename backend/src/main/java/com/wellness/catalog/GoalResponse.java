package com.wellness.catalog;

/** API contract for a goal. Separate from the entity so schema/lazy-loading changes don't leak into the JSON. */
public record GoalResponse(String code, String name, String description) {

    public static GoalResponse from(Goal g) {
        return new GoalResponse(g.getCode(), g.getName(), g.getDescription());
    }
}
