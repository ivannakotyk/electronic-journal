package com.kpi.ejournal.dto.schedule;

import java.util.List;

public record WeeklyScheduleResponse(
        List<DayScheduleDTO> firstWeek,
        List<DayScheduleDTO> secondWeek
) {}
