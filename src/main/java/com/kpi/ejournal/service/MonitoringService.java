package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.monitoring.MonitoringAuditResponse;
import com.kpi.ejournal.dto.monitoring.TeacherMonitoringDetailResponse;
import com.kpi.ejournal.dto.monitoring.TeacherMonitoringResponse;
import com.kpi.ejournal.dto.monitoring.TeacherMonitoringSummaryResponse;
import com.kpi.ejournal.entity.academic.ControlType;
import com.kpi.ejournal.entity.academic.Discipline;
import com.kpi.ejournal.entity.academic.Grade;
import com.kpi.ejournal.entity.academic.GroupEntity;
import com.kpi.ejournal.entity.schedule.ScheduleItem;
import com.kpi.ejournal.entity.schedule.ScheduleType;
import com.kpi.ejournal.entity.user.Teacher;
import com.kpi.ejournal.repository.academic.GradeRepository;
import com.kpi.ejournal.repository.academic.TeachingAssignmentRepository;
import com.kpi.ejournal.repository.schedule.ScheduleRepository;
import com.kpi.ejournal.repository.user.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);
    private static final double LOW_COMPLETION_LIMIT = 60.0;

    private final TeacherRepository teacherRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final GradeRepository gradeRepository;

    public MonitoringService(
            TeacherRepository teacherRepository,
            TeachingAssignmentRepository teachingAssignmentRepository,
            ScheduleRepository scheduleRepository,
            GradeRepository gradeRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.gradeRepository = gradeRepository;
    }

    public TeacherMonitoringSummaryResponse getTeacherMonitoring(
            Long disciplineId,
            Long teacherId,
            Long methodologistId
    ) {
        List<TeacherMonitoringResponse> teachers = teacherRepository.findAll()
                .stream()
                .filter(teacher -> teacherId == null || Objects.equals(teacher.getId(), teacherId))
                .map(teacher -> buildTeacherMonitoring(teacher, disciplineId))
                .filter(Objects::nonNull)
                .sorted(
                        Comparator.comparing(TeacherMonitoringResponse::journalCompletionPercent)
                                .thenComparing(TeacherMonitoringResponse::teacherName)
                )
                .toList();

        int totalUnfilled = teachers.stream()
                .mapToInt(TeacherMonitoringResponse::unfilledLessonsCount)
                .sum();

        int lowCompletion = (int) teachers.stream()
                .filter(t -> t.journalCompletionPercent() < LOW_COMPLETION_LIMIT)
                .count();

        double averageCompletion = teachers.stream()
                .mapToDouble(TeacherMonitoringResponse::journalCompletionPercent)
                .average()
                .orElse(0.0);

        log.info(
                "MONITORING_AUDIT methodologistId={}, disciplineId={}, teacherId={}, resultCount={}",
                methodologistId,
                disciplineId,
                teacherId,
                teachers.size()
        );

        return new TeacherMonitoringSummaryResponse(
                LocalDateTime.now(),
                teachers.size(),
                lowCompletion,
                totalUnfilled,
                round(averageCompletion),
                teachers
        );
    }

    public MonitoringAuditResponse writeAudit(Long methodologistId, String filters) {
        MonitoringAuditResponse audit = new MonitoringAuditResponse(
                methodologistId,
                LocalDateTime.now(),
                filters
        );

        log.info(
                "MONITORING_AUDIT_CONFIRMED methodologistId={}, filters={}",
                methodologistId,
                filters
        );

        return audit;
    }

    private TeacherMonitoringResponse buildTeacherMonitoring(
            Teacher teacher,
            Long disciplineId
    ) {
        List<WorkloadKey> workload = getTeacherWorkload(teacher, disciplineId);

        if (disciplineId != null && workload.isEmpty()) {
            return null;
        }

        List<TeacherMonitoringDetailResponse> details = workload.stream()
                .map(key -> buildDetail(teacher, key))
                .toList();

        List<Grade> teacherGrades = gradeRepository.findByTeacherId(teacher.getId())
                .stream()
                .filter(g -> disciplineId == null || Objects.equals(g.getDiscipline().getId(), disciplineId))
                .toList();

        int expectedCells = details.stream()
                .mapToInt(TeacherMonitoringDetailResponse::expectedGradeCells)
                .sum();

        int filledCells = details.stream()
                .mapToInt(TeacherMonitoringDetailResponse::filledGradeCells)
                .sum();

        int unfilledLessons = details.stream()
                .mapToInt(TeacherMonitoringDetailResponse::unfilledLessonsCount)
                .sum();

        LocalDate lastGradeDate = teacherGrades.stream()
                .map(Grade::getGradeDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new TeacherMonitoringResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getPosition(),
                teacher.getLastLoginAt(),
                lastGradeDate,
                percent(filledCells, expectedCells),
                unfilledLessons,
                filledCells,
                expectedCells,
                teacherGrades.size(),
                (int) teacherGrades.stream().filter(g -> g.getControlType() == ControlType.CURRENT).count(),
                (int) teacherGrades.stream().filter(g -> g.getControlType() == ControlType.SEMESTER).count(),
                details
        );
    }

    private List<WorkloadKey> getTeacherWorkload(Teacher teacher, Long disciplineId) {
        Stream<WorkloadKey> fromAssignments = teachingAssignmentRepository
                .findByTeacherId(teacher.getId())
                .stream()
                .map(a -> new WorkloadKey(a.getGroup(), a.getDiscipline()));

        Stream<WorkloadKey> fromSchedule = scheduleRepository.findAll()
                .stream()
                .filter(s -> s.getTeacher() != null)
                .filter(s -> Objects.equals(s.getTeacher().getId(), teacher.getId()))
                .map(s -> new WorkloadKey(s.getGroup(), s.getDiscipline()));

        Stream<WorkloadKey> fromGrades = gradeRepository.findByTeacherId(teacher.getId())
                .stream()
                .filter(g -> g.getStudent() != null && g.getStudent().getGroup() != null)
                .map(g -> new WorkloadKey(g.getStudent().getGroup(), g.getDiscipline()));

        return Stream.of(fromAssignments, fromSchedule, fromGrades)
                .flatMap(s -> s)
                .filter(key -> key.group() != null && key.discipline() != null)
                .filter(key -> disciplineId == null || Objects.equals(key.discipline().getId(), disciplineId))
                .distinct()
                .sorted(
                        Comparator.comparing((WorkloadKey key) -> key.discipline().getName())
                                .thenComparing(key -> key.group().getCode())
                )
                .toList();
    }

    private TeacherMonitoringDetailResponse buildDetail(
            Teacher teacher,
            WorkloadKey key
    ) {
        GroupEntity group = key.group();
        Discipline discipline = key.discipline();

        int studentsCount = group.getStudents() == null
                ? 0
                : group.getStudents().size();

        List<Grade> grades = gradeRepository
                .findByStudentGroupIdAndDisciplineIdAndTeacherId(
                        group.getId(),
                        discipline.getId(),
                        teacher.getId()
                );

        int filledCells = (int) grades.stream()
                .map(g -> g.getStudent().getId() + ":" + g.getControlType().name())
                .distinct()
                .count();

        int expectedCells = studentsCount * 2;

        List<ScheduleItem> scheduleItems = scheduleRepository.findAll()
                .stream()
                .filter(s -> s.getTeacher() != null)
                .filter(s -> Objects.equals(s.getTeacher().getId(), teacher.getId()))
                .filter(s -> Objects.equals(s.getGroup().getId(), group.getId()))
                .filter(s -> Objects.equals(s.getDiscipline().getId(), discipline.getId()))
                .filter(s -> s.getScheduleType() == ScheduleType.CLASS)
                .toList();

        int scheduleLessons = scheduleItems.size();

        int filledLessonDates = (int) grades.stream()
                .filter(g -> g.getControlType() == ControlType.CURRENT)
                .map(Grade::getGradeDate)
                .distinct()
                .count();

        int unfilledLessons = scheduleLessons == 0
                ? 0
                : Math.max(scheduleLessons - filledLessonDates, 0);

        LocalDate lastGradeDate = grades.stream()
                .map(Grade::getGradeDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new TeacherMonitoringDetailResponse(
                group.getId(),
                group.getCode(),
                discipline.getId(),
                discipline.getName(),
                studentsCount,
                scheduleLessons,
                filledCells,
                expectedCells,
                percent(filledCells, expectedCells),
                unfilledLessons,
                lastGradeDate
        );
    }

    private double percent(int filled, int expected) {
        if (expected <= 0) return 0.0;
        return round((filled * 100.0) / expected);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record WorkloadKey(GroupEntity group, Discipline discipline) {}
}