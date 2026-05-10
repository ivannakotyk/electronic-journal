package com.kpi.ejournal.repository.academic;

import com.kpi.ejournal.entity.academic.ControlType;
import com.kpi.ejournal.entity.academic.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByStudentIdAndControlType(Long studentId, ControlType controlType);

    List<Grade> findByDisciplineId(Long disciplineId);

    List<Grade> findByTeacherId(Long teacherId);

    List<Grade> findByStudentGroupIdAndDisciplineIdAndTeacherId(
            Long groupId,
            Long disciplineId,
            Long teacherId
    );

    List<Grade> findByStudentGroupIdAndControlType(
            Long groupId,
            ControlType controlType
    );

    List<Grade> findByStudentGroupIdAndTeacherIdAndControlType(
            Long groupId,
            Long teacherId,
            ControlType controlType
    );

    List<Grade> findByStudentGroupIdAndDisciplineIdAndControlType(
            Long groupId,
            Long disciplineId,
            ControlType controlType
    );

    List<Grade> findByStudentGroupIdAndDisciplineIdAndTeacherIdAndControlType(
            Long groupId,
            Long disciplineId,
            Long teacherId,
            ControlType controlType
    );
}