package com.kpi.ejournal.dto;

import java.util.List;

public record DayScheduleDTO(
        Integer dayOfWeek,
        String dayName,
        List<ScheduleItemResponse> lessons
) {}