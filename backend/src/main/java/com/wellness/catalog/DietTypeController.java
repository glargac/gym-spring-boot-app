package com.wellness.catalog;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diet-types")
public class DietTypeController {

    private final DietTypeRepository repository;

    public DietTypeController(DietTypeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DietTypeResponse> list() {
        return repository.findAll(Sort.by("name")).stream().map(DietTypeResponse::from).toList();
    }
}
