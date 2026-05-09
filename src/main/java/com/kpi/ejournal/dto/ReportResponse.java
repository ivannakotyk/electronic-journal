package com.kpi.ejournal.dto;
import java.time.LocalDateTime;
public record ReportResponse(
        Long id,
        LocalDateTime createdAt,
        String period,
        Double averageScore,
        Long groupId,
        String groupCode,
        Long disciplineId,
        String disciplineName,
        Long teacherId,
        String teacherName,
        Long methodologistId,
        String methodologistName) {}
