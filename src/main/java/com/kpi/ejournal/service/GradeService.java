package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.*;
import com.kpi.ejournal.entity.*;
import com.kpi.ejournal.exception.NotFoundException;
import com.kpi.ejournal.repository.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DisciplineRepository disciplineRepository;
    private final MapperService mapperService;

    public GradeService(
            GradeRepository gradeRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            DisciplineRepository disciplineRepository,
            MapperService mapperService
    ) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.disciplineRepository = disciplineRepository;
        this.mapperService = mapperService;
    }

    public GradeResponse addGrade(CreateGradeRequest request) {
        Grade grade = new Grade();
        apply(grade, request);
        return mapperService.toGradeResponse(gradeRepository.save(grade));
    }

    public GradeResponse editGrade(Long id, CreateGradeRequest request) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Оцінку не знайдено"));

        apply(grade, request);

        return mapperService.toGradeResponse(gradeRepository.save(grade));
    }

    public List<GradeResponse> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentId(studentId)
                .stream()
                .sorted(Comparator.comparing(Grade::getGradeDate).reversed())
                .map(mapperService::toGradeResponse)
                .toList();
    }

    public AverageResponse getStudentAverage(Long studentId) {
        double average = gradeRepository
                .findByStudentIdAndControlType(studentId, ControlType.SEMESTER)
                .stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0);

        return new AverageResponse(studentId, average);
    }

    public List<CurrentControlSummaryResponse> getCurrentControlSummary(Long studentId) {
        List<Grade> currentGrades = gradeRepository
                .findByStudentIdAndControlType(studentId, ControlType.CURRENT);

        Map<Discipline, List<Grade>> groupedByDiscipline = currentGrades
                .stream()
                .collect(Collectors.groupingBy(Grade::getDiscipline));

        return groupedByDiscipline.entrySet()
                .stream()
                .map(entry -> {
                    Discipline discipline = entry.getKey();
                    List<Grade> grades = entry.getValue();

                    double totalScore = grades.stream()
                            .mapToDouble(Grade::getValue)
                            .sum();

                    return new CurrentControlSummaryResponse(
                            discipline.getId(),
                            discipline.getName(),
                            totalScore,
                            grades.size()
                    );
                })
                .sorted(Comparator.comparing(CurrentControlSummaryResponse::disciplineName))
                .toList();
    }

    private void apply(Grade grade, CreateGradeRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("Студента не знайдено"));

        Teacher teacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new NotFoundException("Викладача не знайдено"));

        Discipline discipline = disciplineRepository.findById(request.disciplineId())
                .orElseThrow(() -> new NotFoundException("Дисципліну не знайдено"));

        grade.setValue(request.value());
        grade.setGradeDate(request.gradeDate());
        grade.setControlType(ControlType.valueOf(request.controlType().toUpperCase()));
        grade.setComment(request.comment());
        grade.setStudent(student);
        grade.setTeacher(teacher);
        grade.setDiscipline(discipline);
    }
}