package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.discipline.CreateDisciplineRequest;
import com.kpi.ejournal.dto.discipline.DisciplineResponse;
import com.kpi.ejournal.service.DisciplineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/disciplines")
public class DisciplineController {

    private final DisciplineService disciplineService;

    public DisciplineController(DisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }

    @GetMapping
    public List<DisciplineResponse> getAll() {
        return disciplineService.getAll();
    }

    @PostMapping
    public DisciplineResponse create(@Valid @RequestBody CreateDisciplineRequest request) {
        return disciplineService.create(request);
    }
}