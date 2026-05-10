package com.kpi.ejournal.dto.schedule;

import java.util.List;

public record DayScheduleDTO(
        Integer dayOfWeek,
        String dayName,
        List<ScheduleItemResponse> lessons
) {}