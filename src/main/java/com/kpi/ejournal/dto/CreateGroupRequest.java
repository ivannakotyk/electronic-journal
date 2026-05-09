package com.kpi.ejournal.dto;
import jakarta.validation.constraints.*;
public record CreateGroupRequest(
        @NotBlank String code,
        @NotNull @Min(1) Integer course,
        @NotBlank String specialty) {}
