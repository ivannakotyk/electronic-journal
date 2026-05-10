package com.kpi.ejournal.dto.monitoring;

import java.time.LocalDateTime;
import java.util.List;

public record TeacherMonitoringSummaryResponse(
        LocalDateTime checkedAt,
        Integer teachersCount,
        Integer lowCompletionTeachersCount,
        Integer totalUnfilledLessonsCount,
        Double averageCompletionPercent,
        List<TeacherMonitoringResponse> teachers
) {}