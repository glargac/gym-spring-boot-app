package com.wellness.plan;

import com.wellness.security.OwnerOrAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Same rule as preferences: the owner or an ADMIN. */
@RestController
@RequestMapping("/api/users/{userId}/recommendation")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping
    @OwnerOrAdmin
    public RecommendationResponse get(@PathVariable Long userId) {
        return service.recommend(userId);
    }
}
