package com.kpi.ejournal.repository.academic;

import com.kpi.ejournal.entity.academic.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {
    List<TeachingAssignment> findByTeacherId(Long teacherId);
    List<TeachingAssignment> findByGroupId(Long groupId);

    boolean existsByTeacherIdAndGroupIdAndDisciplineId(
            Long teacherId,
            Long groupId,
            Long disciplineId
    );
}