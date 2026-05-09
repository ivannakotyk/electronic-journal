package com.kpi.ejournal.dto;

public record ReportChartItemResponse(
        String label,
        long count,
        double percent
) {}
