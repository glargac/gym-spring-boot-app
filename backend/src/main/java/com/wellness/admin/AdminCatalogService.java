package com.wellness.admin;

import com.wellness.catalog.DietType;
import com.wellness.catalog.DietTypeRepository;
import com.wellness.catalog.DietTypeResponse;
import com.wellness.catalog.Goal;
import com.wellness.catalog.GoalRepository;
import com.wellness.catalog.GoalResponse;
import com.wellness.catalog.TrainingLocation;
import com.wellness.catalog.TrainingLocationRepository;
import com.wellness.catalog.TrainingLocationResponse;
import com.wellness.web.ConflictException;
import com.wellness.web.InvalidFieldException;
import com.wellness.web.NotFoundException;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogService {

    private static final Pattern CODE = Pattern.compile("^[A-Z][A-Z0-9_]{1,29}$");

    private final GoalRepository goals;
    private final DietTypeRepository dietTypes;
    private final TrainingLocationRepository locations;

    public AdminCatalogService(GoalRepository goals, DietTypeRepository dietTypes, TrainingLocationRepository locations) {
        this.goals = goals;
        this.dietTypes = dietTypes;
        this.locations = locations;
    }

    /** PUT = upsert at a known URL: the admin chooses the code, so creating and updating are the same idempotent call. */
    @Transactional
    public GoalResponse saveGoal(String code, CatalogItemRequest r) {
        checkCode(code);
        Goal goal = goals.findById(code).orElse(null);
        if (goal == null) {
            goal = new Goal(code, r.name().trim(), r.description());
        } else {
            goal.update(r.name().trim(), r.description());
        }
        return GoalResponse.from(goals.save(goal));
    }

    @Transactional
    public DietTypeResponse saveDietType(String code, CatalogItemRequest r) {
        checkCode(code);
        DietType diet = dietTypes.findById(code).orElse(null);
        if (diet == null) {
            diet = new DietType(code, r.name().trim(), r.description());
        } else {
            diet.update(r.name().trim(), r.description());
        }
        return DietTypeResponse.from(dietTypes.save(diet));
    }

    @Transactional
    public TrainingLocationResponse saveLocation(String code, CatalogItemRequest r) {
        checkCode(code);
        TrainingLocation location = locations.findById(code).orElse(null);
        if (location == null) {
            location = new TrainingLocation(code, r.name().trim());
        } else {
            location.update(r.name().trim());
        }
        return TrainingLocationResponse.from(locations.save(location));
    }

    @Transactional
    public void deleteGoal(String code) {
        deleteIfUnused(goals, code, "goal");
    }

    @Transactional
    public void deleteDietType(String code) {
        deleteIfUnused(dietTypes, code, "diet type");
    }

    @Transactional
    public void deleteLocation(String code) {
        deleteIfUnused(locations, code, "training location");
    }

    /**
     * The DB foreign keys are the real guard: if any user, template or plan still points at this row the DELETE fails.
     * We flush inside the try so that failure surfaces here (and becomes a clear 409) instead of at commit time.
     */
    private <T> void deleteIfUnused(JpaRepository<T, String> repository, String code, String label) {
        T entity = repository.findById(code).orElseThrow(() -> new NotFoundException("Unknown " + label + " '" + code + "'"));
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("The " + label + " '" + code + "' is still in use and cannot be deleted");
        }
    }

    private static void checkCode(String code) {
        if (!CODE.matcher(code).matches()) {
            throw new InvalidFieldException("code", "must be 2-30 characters: capital letters, digits and underscores, starting with a letter");
        }
    }
}
