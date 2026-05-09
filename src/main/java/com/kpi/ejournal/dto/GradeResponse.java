package com.kpi.ejournal.dto;
import java.time.LocalDate;
public record GradeResponse(
        Long id,
        Double value,
        LocalDate gradeDate,
        String controlType,
        String comment,
        Long studentId,
        String studentName,
        Long teacherId,
        String teacherName,
        Long disciplineId,
        String disciplineName) {}
