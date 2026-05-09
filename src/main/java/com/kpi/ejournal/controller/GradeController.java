package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.*;
import com.kpi.ejournal.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService)
    {
        this.gradeService = gradeService;
    }

    @PostMapping
    public GradeResponse addGrade(@Valid @RequestBody CreateGradeRequest request)
    {
        return gradeService.addGrade(request);
    }

    @PutMapping("/{id}")
    public GradeResponse editGrade(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateGradeRequest request
    )
    {
        return gradeService.editGrade(id, request);
    }

    @GetMapping("/student/{studentId}")
    public List<GradeResponse> getStudentGrades(
            @PathVariable("studentId") Long studentId
    )
    {
        return gradeService.getStudentGrades(studentId);
    }

    @GetMapping("/student/{studentId}/average")
    public AverageResponse getAverage(
            @PathVariable("studentId") Long studentId
    )
    {
        return gradeService.getStudentAverage(studentId);
    }

    @GetMapping("/student/{studentId}/current-summary")
    public List<CurrentControlSummaryResponse> getCurrentControlSummary(
            @PathVariable("studentId") Long studentId
    )
    {
        return gradeService.getCurrentControlSummary(studentId);
    }
}