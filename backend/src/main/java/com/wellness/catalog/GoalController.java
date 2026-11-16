package com.wellness.catalog;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalRepository repository;

    public GoalController(GoalRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<GoalResponse> list() {
        return repository.findAll(Sort.by("name")).stream().map(GoalResponse::from).toList();
    }
}
