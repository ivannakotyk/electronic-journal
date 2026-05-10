package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.monitoring.MonitoringAuditResponse;
import com.kpi.ejournal.dto.monitoring.TeacherMonitoringSummaryResponse;
import com.kpi.ejournal.service.MonitoringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/teachers")
    public TeacherMonitoringSummaryResponse getTeacherMonitoring(
            @RequestParam(value = "disciplineId", required = false) Long disciplineId,
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "methodologistId", required = false) Long methodologistId
    ) {
        return monitoringService.getTeacherMonitoring(
                disciplineId,
                teacherId,
                methodologistId
        );
    }

    @PostMapping("/audit")
    public MonitoringAuditResponse writeAudit(
            @RequestParam(value = "methodologistId", required = false) Long methodologistId,
            @RequestParam(value = "filters", required = false) String filters
    ) {
        return monitoringService.writeAudit(methodologistId, filters);
    }
}