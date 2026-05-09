package com.kpi.ejournal.dto;
import java.time.*;
public record ScheduleItemResponse(
        Long id,
        String scheduleType,
        LocalDate date,
        LocalTime time,
        String room,
        Long groupId,
        String groupCode,
        Long disciplineId,
        String disciplineName,
        Long teacherId,
        String teacherName) {}
