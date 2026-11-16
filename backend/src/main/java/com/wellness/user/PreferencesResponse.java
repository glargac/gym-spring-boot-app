package com.wellness.user;

import java.math.BigDecimal;

public record PreferencesResponse(Long userId, String goalCode, String locationCode, String dietTypeCode,
                                  int daysPerWeek, BigDecimal hoursPerDay, String excludedFoods) {

    /** Call inside a transaction: the lazy goal/location/diet references are read here, and open-in-view is off. */
    public static PreferencesResponse from(UserPreference p) {
        return new PreferencesResponse(p.getUserId(), p.getGoal().getCode(), p.getLocation().getCode(),
                p.getDietType().getCode(), p.getDaysPerWeek(), p.getHoursPerDay(), p.getExcludedFoods());
    }
}
