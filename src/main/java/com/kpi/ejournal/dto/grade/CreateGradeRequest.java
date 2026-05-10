package com.kpi.ejournal.dto.grade;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record CreateGradeRequest(
        @NotNull @Min(0) @Max(100) Double value,
        @NotNull LocalDate gradeDate,
        @NotNull String controlType,
        String comment,
        @NotNull Long studentId,
        @NotNull Long teacherId,
        @NotNull Long disciplineId) {}
