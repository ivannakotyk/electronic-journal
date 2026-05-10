package com.kpi.ejournal.dto.group;
import jakarta.validation.constraints.*;
public record CreateGroupRequest(
        @NotBlank String code,
        @NotNull @Min(1) Integer course,
        @NotBlank String specialty) {}
