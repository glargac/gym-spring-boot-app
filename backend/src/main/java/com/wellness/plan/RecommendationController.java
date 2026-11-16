package com.wellness.plan;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Open for now, like the preferences endpoint: the security step adds the ownership check. */
@RestController
@RequestMapping("/api/users/{userId}/recommendation")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping
    public RecommendationResponse get(@PathVariable Long userId) {
        return service.recommend(userId);
    }
}
