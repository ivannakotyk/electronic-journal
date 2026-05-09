package com.kpi.ejournal.dto;
import jakarta.validation.constraints.*;
public record UpdateUserRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        String position,
        String studentCardNumber,
        Long groupId) {}
