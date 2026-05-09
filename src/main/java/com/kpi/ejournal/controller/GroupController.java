package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.*;
import com.kpi.ejournal.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<GroupResponse> getAll() {
        return groupService.getAll();
    }

    @PostMapping
    public GroupResponse create(@Valid @RequestBody CreateGroupRequest request) {
        return groupService.create(request);
    }
}