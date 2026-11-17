package com.wellness.admin;

import com.wellness.catalog.DietTypeResponse;
import com.wellness.catalog.GoalResponse;
import com.wellness.catalog.TrainingLocationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only (the whole /api/admin/** tree is protected in SecurityConfig). Reading stays on the public GET endpoints. */
@RestController
@RequestMapping("/api/admin")
public class AdminCatalogController {

    private final AdminCatalogService service;

    public AdminCatalogController(AdminCatalogService service) {
        this.service = service;
    }

    @PutMapping("/goals/{code}")
    public GoalResponse saveGoal(@PathVariable String code, @Valid @RequestBody CatalogItemRequest request) {
        return service.saveGoal(code, request);
    }

    @DeleteMapping("/goals/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(@PathVariable String code) {
        service.deleteGoal(code);
    }

    @PutMapping("/diet-types/{code}")
    public DietTypeResponse saveDietType(@PathVariable String code, @Valid @RequestBody CatalogItemRequest request) {
        return service.saveDietType(code, request);
    }

    @DeleteMapping("/diet-types/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDietType(@PathVariable String code) {
        service.deleteDietType(code);
    }

    @PutMapping("/training-locations/{code}")
    public TrainingLocationResponse saveLocation(@PathVariable String code, @Valid @RequestBody CatalogItemRequest request) {
        return service.saveLocation(code, request);
    }

    @DeleteMapping("/training-locations/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable String code) {
        service.deleteLocation(code);
    }
}
