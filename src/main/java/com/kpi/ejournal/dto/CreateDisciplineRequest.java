package com.kpi.ejournal.dto;
import jakarta.validation.constraints.*;
public record CreateDisciplineRequest(
        @NotBlank String name,
        @NotNull @Min(1) Integer semester) {}
