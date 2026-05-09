package com.kpi.ejournal.dto;

public record CurrentControlSummaryResponse(
        Long disciplineId,
        String disciplineName,
        Double totalScore,
        Integer gradesCount
) {}