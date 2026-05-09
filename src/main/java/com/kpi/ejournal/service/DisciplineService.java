package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.*;
import com.kpi.ejournal.entity.Discipline;
import com.kpi.ejournal.repository.DisciplineRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DisciplineService {
    private final DisciplineRepository disciplineRepository;
    private final MapperService mapperService;
    public DisciplineService(DisciplineRepository disciplineRepository, MapperService mapperService) {
        this.disciplineRepository = disciplineRepository; this.mapperService = mapperService;
    }
    public List<DisciplineResponse> getAll() { return disciplineRepository.findAll().stream().map(mapperService::toDisciplineResponse).toList(); }
    public DisciplineResponse create(CreateDisciplineRequest request) {
        Discipline discipline = new Discipline();
        discipline.setName(request.name()); discipline.setSemester(request.semester());
        return mapperService.toDisciplineResponse(disciplineRepository.save(discipline));
    }
}
