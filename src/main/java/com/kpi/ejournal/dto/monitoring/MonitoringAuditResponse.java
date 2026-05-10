package com.kpi.ejournal.dto.monitoring;

import java.time.LocalDateTime;

public record MonitoringAuditResponse(
        Long methodologistId,
        LocalDateTime checkedAt,
        String filters
) {}