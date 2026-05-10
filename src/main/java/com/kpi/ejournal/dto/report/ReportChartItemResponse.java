package com.kpi.ejournal.dto.report;

public record ReportChartItemResponse(
        String label,
        long count,
        double percent
) {}
