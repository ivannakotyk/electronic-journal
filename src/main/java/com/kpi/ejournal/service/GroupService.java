package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.group.CreateGroupRequest;
import com.kpi.ejournal.dto.group.GroupResponse;
import com.kpi.ejournal.entity.academic.GroupEntity;
import com.kpi.ejournal.exception.BadRequestException;
import com.kpi.ejournal.repository.academic.GroupRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final MapperService mapperService;
    public GroupService(GroupRepository groupRepository, MapperService mapperService) {
        this.groupRepository = groupRepository;
        this.mapperService = mapperService;
    }
    public List<GroupResponse> getAll() {
        return groupRepository.findAll().stream().map(mapperService::toGroupResponse).toList();
    }
    public GroupResponse create(CreateGroupRequest request) {
        if (groupRepository.existsByCode(request.code()))
            throw new BadRequestException("Група з таким кодом уже існує");
        GroupEntity group = new GroupEntity();
        group.setCode(request.code());
        group.setCourse(request.course());
        group.setSpecialty(request.specialty());
        return mapperService.toGroupResponse(groupRepository.save(group));
    }
}
