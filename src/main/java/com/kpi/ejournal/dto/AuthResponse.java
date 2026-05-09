package com.kpi.ejournal.dto;
public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String role)
{}