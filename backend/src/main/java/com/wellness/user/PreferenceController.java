package com.wellness.user;

import com.wellness.security.OwnerOrAdmin;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A user can only touch their own preferences; an ADMIN can touch anyone's (see OwnerOrAdmin). */
@RestController
@RequestMapping("/api/users/{userId}/preferences")
public class PreferenceController {

    private final PreferenceService service;

    public PreferenceController(PreferenceService service) {
        this.service = service;
    }

    @PutMapping
    @OwnerOrAdmin
    public PreferencesResponse save(@PathVariable Long userId, @Valid @RequestBody PreferencesRequest request) {
        return service.save(userId, request);
    }

    @GetMapping
    @OwnerOrAdmin
    public PreferencesResponse get(@PathVariable Long userId) {
        return service.get(userId);
    }
}
