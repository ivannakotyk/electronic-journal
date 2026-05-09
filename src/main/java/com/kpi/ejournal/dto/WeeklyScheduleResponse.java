package com.kpi.ejournal.dto;

import java.util.List;

public record WeeklyScheduleResponse(
        List<DayScheduleDTO> firstWeek,
        List<DayScheduleDTO> secondWeek
) {}
