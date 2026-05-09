package com.kpi.ejournal.repository;

import com.kpi.ejournal.entity.ScheduleItem;
import com.kpi.ejournal.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByGroupIdOrderByDayOfWeekAscTimeAsc(Long groupId);

    List<ScheduleItem> findByTeacherIdAndDisciplineIdAndGroupIdAndScheduleType(
            Long teacherId,
            Long disciplineId,
            Long groupId,
            ScheduleType scheduleType
    );
}