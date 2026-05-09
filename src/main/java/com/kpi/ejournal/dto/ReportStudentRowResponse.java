package com.kpi.ejournal.dto;

public record ReportStudentRowResponse(
        Long studentId,
        String studentName,
        String groupCode,
        String disciplineName,
        Double semesterScore
) {}
