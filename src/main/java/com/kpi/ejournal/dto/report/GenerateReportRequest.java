package com.kpi.ejournal.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateReportRequest(
        @NotBlank String period,
        @NotNull Long groupId,
        @NotNull Long disciplineId,
        Long teacherId,
        Long methodologistId
) {}