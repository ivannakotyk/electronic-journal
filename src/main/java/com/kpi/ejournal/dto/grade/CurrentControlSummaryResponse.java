package com.kpi.ejournal.dto.grade;

public record CurrentControlSummaryResponse(
        Long disciplineId,
        String disciplineName,
        Double totalScore,
        Integer gradesCount
) {}