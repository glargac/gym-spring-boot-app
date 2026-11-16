package com.wellness.user;

import com.wellness.catalog.DietTypeRepository;
import com.wellness.catalog.GoalRepository;
import com.wellness.catalog.TrainingLocationRepository;
import com.wellness.web.NotFoundException;
import com.wellness.web.UnknownCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceService {

    private final UserRepository users;
    private final UserPreferenceRepository preferences;
    private final GoalRepository goals;
    private final TrainingLocationRepository locations;
    private final DietTypeRepository dietTypes;

    public PreferenceService(UserRepository users, UserPreferenceRepository preferences, GoalRepository goals,
                             TrainingLocationRepository locations, DietTypeRepository dietTypes) {
        this.users = users;
        this.preferences = preferences;
        this.goals = goals;
        this.locations = locations;
        this.dietTypes = dietTypes;
    }

    /** Upsert: PUT means "this is the full state", so create if missing and replace if present (idempotent). */
    @Transactional
    public PreferencesResponse save(Long userId, PreferencesRequest r) {
        AppUser user = users.findById(userId).orElseThrow(() -> userNotFound(userId));

        var goal = goals.findById(r.goalCode()).orElseThrow(() -> new UnknownCodeException("goalCode", r.goalCode()));
        var location = locations.findById(r.locationCode())
                .orElseThrow(() -> new UnknownCodeException("locationCode", r.locationCode()));
        var diet = dietTypes.findById(r.dietTypeCode())
                .orElseThrow(() -> new UnknownCodeException("dietTypeCode", r.dietTypeCode()));

        UserPreference pref = preferences.findById(userId).orElse(null);
        if (pref == null) {
            pref = new UserPreference(user, goal, location, r.daysPerWeek(), r.hoursPerDay(), diet, r.excludedFoods());
        } else {
            pref.change(goal, location, r.daysPerWeek(), r.hoursPerDay(), diet, r.excludedFoods()); // dirty checking writes the UPDATE
        }
        return PreferencesResponse.from(preferences.save(pref));
    }

    /** readOnly lets Hibernate skip dirty checking and gives the DB driver a hint. */
    @Transactional(readOnly = true)
    public PreferencesResponse get(Long userId) {
        return preferences.findById(userId)
                .map(PreferencesResponse::from)
                .orElseThrow(() -> new NotFoundException("No preferences saved for user " + userId));
    }

    private static NotFoundException userNotFound(Long userId) {
        return new NotFoundException("User " + userId + " not found");
    }
}
