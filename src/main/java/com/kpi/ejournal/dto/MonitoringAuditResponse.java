package com.kpi.ejournal.dto;

import java.time.LocalDateTime;

public record MonitoringAuditResponse(
        Long methodologistId,
        LocalDateTime checkedAt,
        String filters
) {}