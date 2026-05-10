package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.discipline.DisciplineResponse;
import com.kpi.ejournal.dto.grade.GradeResponse;
import com.kpi.ejournal.dto.group.GroupResponse;
import com.kpi.ejournal.dto.report.ReportResponse;
import com.kpi.ejournal.dto.schedule.ScheduleItemResponse;
import com.kpi.ejournal.dto.user.UserResponse;
import com.kpi.ejournal.entity.academic.Discipline;
import com.kpi.ejournal.entity.academic.Grade;
import com.kpi.ejournal.entity.academic.GroupEntity;
import com.kpi.ejournal.entity.report.Report;
import com.kpi.ejournal.entity.schedule.ScheduleItem;
import com.kpi.ejournal.entity.user.Student;
import com.kpi.ejournal.entity.user.Teacher;
import com.kpi.ejournal.entity.user.User;
import org.springframework.stereotype.Service;
@Service
public class MapperService {
    public UserResponse toUserResponse(User user) {
        String position = null,
                studentCardNumber = null,
                groupCode = null;
        Long groupId = null;
        if (user instanceof Teacher teacher) position = teacher.getPosition();
        if (user instanceof Student student) {
            studentCardNumber = student.getStudentCardNumber();
            if (student.getGroup() != null) {
                groupId = student.getGroup().getId();
                groupCode = student.getGroup().getCode();
            }
        }
        return new UserResponse(user.getId(),
                user.getFullName(),
                user.getLogin(),
                user.getEmail(),
                user.getRole().name(),
                position,
                studentCardNumber,
                groupId,
                groupCode);
    }

    public GroupResponse toGroupResponse(GroupEntity group)
    {
        return new GroupResponse(group.getId(),
                group.getCode(),
                group.getCourse(),
                group.getSpecialty());
    }

    public DisciplineResponse toDisciplineResponse(Discipline d)
    {
        return new DisciplineResponse(d.getId(),
                d.getName(),
                d.getSemester());
    }

    public GradeResponse toGradeResponse(Grade g)
    {
        return new GradeResponse(g.getId(),
                g.getValue(),
                g.getGradeDate(),
                g.getControlType().name(),
                g.getComment(),
                g.getStudent().getId(),
                g.getStudent().getFullName(),
                g.getTeacher().getId(),
                g.getTeacher().getFullName(),
                g.getDiscipline().getId(),
                g.getDiscipline().getName());
    }

    public ScheduleItemResponse toScheduleItemResponse(ScheduleItem i) {
        return new ScheduleItemResponse(
                i.getId(),
                i.getScheduleType().name(),
                null,
                i.getTime(),
                i.getRoom(),
                i.getGroup().getId(),
                i.getGroup().getCode(),
                i.getDiscipline().getId(),
                i.getDiscipline().getName(),
                i.getTeacher() != null ? i.getTeacher().getId() : null,
                i.getTeacher() != null ? i.getTeacher().getFullName() : "—"
        );
    }

    public ReportResponse toReportResponse(Report r) {
        return new ReportResponse(r.getId(),
                r.getCreatedAt(),
                r.getPeriod(),
                r.getAverageScore(),
                r.getGroup() != null ?
                        r.getGroup().getId() : null,
                r.getGroup() != null ?
                        r.getGroup().getCode() : null,
                r.getDiscipline() != null ?
                        r.getDiscipline().getId() : null,
                r.getDiscipline() != null ?
                        r.getDiscipline().getName() : null,
                r.getTeacher() != null ?
                        r.getTeacher().getId() : null,
                r.getTeacher() != null ?
                        r.getTeacher().getFullName() : null,
                r.getMethodologist() != null ?
                        r.getMethodologist().getId() : null,
                r.getMethodologist() != null ?
                        r.getMethodologist().getFullName() : null);
    }
}