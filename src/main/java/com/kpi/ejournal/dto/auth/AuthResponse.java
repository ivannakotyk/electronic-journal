package com.kpi.ejournal.dto.auth;
public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String role)
{}