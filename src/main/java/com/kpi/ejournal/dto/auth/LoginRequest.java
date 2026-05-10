package com.kpi.ejournal.dto.auth;
import jakarta.validation.constraints.NotBlank;
public record LoginRequest(
        @NotBlank String login,
        @NotBlank String password) {}
