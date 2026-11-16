package com.wellness.catalog;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training-locations")
public class TrainingLocationController {

    private final TrainingLocationRepository repository;

    public TrainingLocationController(TrainingLocationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TrainingLocationResponse> list() {
        return repository.findAll(Sort.by("name")).stream().map(TrainingLocationResponse::from).toList();
    }
}
