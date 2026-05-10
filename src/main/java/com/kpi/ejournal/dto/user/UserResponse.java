package com.kpi.ejournal.dto.user;
public record UserResponse(
        Long id,
        String fullName,
        String login,
        String email,
        String role,
        String position,
        String studentCardNumber,
        Long groupId,
        String groupCode) {}
