package com.kpi.ejournal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportDetailsResponse(
        Long id,
        LocalDateTime createdAt,
        String period,
        Long groupId,
        String groupCode,
        Long disciplineId,
        String disciplineName,
        Long teacherId,
        String teacherName,
        Long methodologistId,
        String methodologistName,
        Double averageScore,
        int totalStudents,
        int studentsWithSemesterScore,
        int studentsWithoutSemesterScore,
        List<ReportStudentRowResponse> rows,
        List<ReportChartItemResponse> distribution
) {}
