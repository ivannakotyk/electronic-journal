package com.kpi.ejournal.dto.monitoring;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TeacherMonitoringResponse(
        Long teacherId,
        String teacherName,
        String position,
        LocalDateTime lastLoginAt,
        LocalDate lastGradeDate,
        Double journalCompletionPercent,
        Integer unfilledLessonsCount,
        Integer filledGradeCells,
        Integer expectedGradeCells,
        Integer gradesCount,
        Integer currentGradesCount,
        Integer semesterGradesCount,
        List<TeacherMonitoringDetailResponse> details
) {}