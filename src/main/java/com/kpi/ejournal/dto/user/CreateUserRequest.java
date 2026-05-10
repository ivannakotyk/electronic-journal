package com.kpi.ejournal.dto.user;
import jakarta.validation.constraints.*;
public record CreateUserRequest(
        @NotBlank String fullName,
        @NotBlank String login,
        @NotBlank String password,
        @Email @NotBlank String email,
        @NotNull String role,
        String position,
        String studentCardNumber,
        Long groupId) {}
