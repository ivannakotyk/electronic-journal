package com.kpi.ejournal.dto;

import java.time.LocalDate;

public record TeacherMonitoringDetailResponse(
        Long groupId,
        String groupCode,
        Long disciplineId,
        String disciplineName,
        Integer studentsCount,
        Integer scheduleLessonsCount,
        Integer filledGradeCells,
        Integer expectedGradeCells,
        Double completionPercent,
        Integer unfilledLessonsCount,
        LocalDate lastGradeDate
) {}