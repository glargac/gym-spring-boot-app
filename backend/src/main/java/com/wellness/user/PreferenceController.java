package com.wellness.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Open for now: anyone can read or change anyone's preferences. Roadmap step 2 adds the ownership check. */
@RestController
@RequestMapping("/api/users/{userId}/preferences")
public class PreferenceController {

    private final PreferenceService service;

    public PreferenceController(PreferenceService service) {
        this.service = service;
    }

    @PutMapping
    public PreferencesResponse save(@PathVariable Long userId, @Valid @RequestBody PreferencesRequest request) {
        return service.save(userId, request);
    }

    @GetMapping
    public PreferencesResponse get(@PathVariable Long userId) {
        return service.get(userId);
    }
}
