package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.ScheduleItemResponse;
import com.kpi.ejournal.dto.WeeklyScheduleResponse;
import com.kpi.ejournal.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/weekly")
    public WeeklyScheduleResponse getWeeklySchedule(@RequestParam("groupId") Long groupId) {
        return scheduleService.getFullSchedule(groupId);
    }

    @GetMapping("/session")
    public List<ScheduleItemResponse> getSessionSchedule(@RequestParam("groupId") Long groupId) {
        return scheduleService.getSessionSchedule(groupId);
    }
}